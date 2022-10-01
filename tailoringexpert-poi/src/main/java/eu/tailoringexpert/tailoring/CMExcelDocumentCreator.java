/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 Michael Bädorf and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static eu.tailoringexpert.domain.File.*;
import static java.nio.file.Files.newInputStream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;
import static org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT;

/**
 * Create Excel Compliance Matrix file.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class CMExcelDocumentCreator implements DocumentCreator {

    @NonNull
    private Function<String, java.io.File> templateSupplier;

    @NonNull
    private BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    private static final int MAIN_CHAPTER = 1;
    private static final int SUB_CHAPTER = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(String docId,
                               Tailoring tailoring,
                               Map<String, String> placeholders) {

        try {
            FileBuilder result = builder().name(docId + ".xlsx");

            java.io.File template = templateSupplier.apply(tailoring.getCatalog().getVersion() + "/cm.xlsx");
            try (Workbook wb = new XSSFWorkbook(newInputStream(template.toPath()))) {
                Sheet cmSheet = createCMSheet(wb);

                Catalog<TailoringRequirement> catalog = tailoring.getCatalog();
                catalog.getToc().getChapters()
                    .forEach(chapter -> addChapter(chapter, 1, cmSheet));

                Collection<DRDElement> drds = new LinkedList<>();
                addDRD(catalog.getToc(), drds, tailoring.getPhases());

                range(0, cmSheet.getRow(0).getPhysicalNumberOfCells())
                    .forEach(cmSheet::autoSizeColumn);

                Sheet drdSheet = createDRDSheet(wb, drds);
                range(0, drdSheet.getRow(0).getPhysicalNumberOfCells())
                    .forEach(drdSheet::autoSizeColumn);

                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    wb.write(os);
                    result.data(os.toByteArray());
                }
            }
            return result.build();
        } catch (Exception e) {
            log.throwing(e);
        }
        return null;

    }

    void addDRD(Chapter<TailoringRequirement> chapter, Collection<DRDElement> rows, Collection<Phase> phases) {
        drdProvider.apply(chapter, phases)
            .entrySet()
            .forEach(entry -> rows.add(DRDElement.builder()
                .title(entry.getKey().getTitle())
                .deliveryDate(entry.getKey().getDeliveryDate())
                .requirements(entry.getValue())
                .number(entry.getKey().getNumber())
                .action(entry.getKey().getAction())
                .build()));
    }

    private Sheet createDRDSheet(Workbook wb, Collection<DRDElement> drds) {
        Sheet result = wb.createSheet("DRD");

        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(GREY_25_PERCENT.index);
        cellStyle.setFillPattern(SOLID_FOREGROUND);
        cellStyle.setWrapText(true);

        Row row = result.createRow((short) 0);
        row.createCell(0).setCellValue("Title");
        row.getCell(0).setCellStyle(cellStyle);
        row.createCell(1).setCellValue("Due Date");
        row.getCell(1).setCellStyle(cellStyle);
        row.createCell(2).setCellValue("A-Req't.");
        row.getCell(2).setCellStyle(cellStyle);
        row.createCell(3).setCellValue("DRD No");
        row.getCell(3).setCellStyle(cellStyle);
        row.createCell(4).setCellValue("DLR Action");
        row.getCell(4).setCellStyle(cellStyle);

        result.getColumnStyle(2).setWrapText(true);
        result.setColumnWidth(2, 20);
        drds.forEach(drd -> addRow(result, drd));

        result.setAutoFilter(new CellRangeAddress(0, 0, 0, 4));

        return result;
    }

    private void addRow(Sheet sheet, DRDElement drd) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);

        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setWrapText(true);

        row.createCell(0).setCellValue(drd.getTitle());
        row.getCell(0).setCellStyle(cellStyle);

        row.createCell(1).setCellValue(drd.getDeliveryDate());
        row.getCell(1).setCellStyle(cellStyle);

        row.createCell(2).setCellValue(drd.getRequirements().stream().collect(joining(", \n")));
        row.getCell(2).setCellStyle(cellStyle);

        row.createCell(3).setCellValue(drd.getNumber());
        row.getCell(3).setCellStyle(cellStyle);

        row.createCell(4).setCellValue(drd.getAction());
        row.getCell(4).setCellStyle(cellStyle);
    }

    private void addChapter(Chapter<TailoringRequirement> chapter, int level, Sheet sheet) {
        addRow(sheet, level, chapter.getNumber(), chapter.getName());
        if (nonNull(chapter.getChapters())) {
            AtomicInteger nextLevel = new AtomicInteger(level + 1);
            chapter.getChapters()
                .forEach(subChapter -> addChapter(subChapter, nextLevel.get(), sheet));
        }
    }

    private Sheet createCMSheet(Workbook wb) {
        Sheet result = wb.createSheet("CM");

        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFillForegroundColor(GREY_25_PERCENT.index);
        headerCellStyle.setFillPattern(SOLID_FOREGROUND);

        Row row = result.createRow((short) 0);
        row.createCell(0).setCellValue("DLR  Requirem. para.");
        row.getCell(0).setCellStyle(headerCellStyle);
        row.createCell(1).setCellValue("Title");
        row.getCell(1).setCellStyle(headerCellStyle);
        row.createCell(2).setCellValue("Compliance  Status");
        row.getCell(2).setCellStyle(headerCellStyle);
        row.createCell(3).setCellValue("Cross Reference");
        row.getCell(3).setCellStyle(headerCellStyle);
        row.createCell(4).setCellValue("Remarks");
        row.getCell(4).setCellStyle(headerCellStyle);

        result.setAutoFilter(new CellRangeAddress(0, 0, 0, 4));

        return result;
    }

    private void addRow(Sheet sheet, int level, String chapter, String title) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);

        CellStyle cellStyle;
        if (MAIN_CHAPTER == level) {
            cellStyle = createCellStyle(sheet, IndexedColors.LIGHT_BLUE);
        } else if (SUB_CHAPTER == level) {
            cellStyle = createCellStyle(sheet, IndexedColors.LIGHT_GREEN);
        } else {
            cellStyle = sheet.getWorkbook().createCellStyle();
        }

        row.createCell(0).setCellValue(chapter);
        row.getCell(0).setCellStyle(cellStyle);

        row.createCell(1).setCellValue(title);
        row.getCell(1).setCellStyle(cellStyle);

        row.createCell(2).setCellStyle(cellStyle);
        row.createCell(3).setCellStyle(cellStyle);
        row.createCell(4).setCellStyle(cellStyle);

    }

    private CellStyle createCellStyle(Sheet sheet, IndexedColors farbe) {
        CellStyle result = sheet.getWorkbook().createCellStyle();
        result.setFillForegroundColor(farbe.index);
        result.setFillPattern(SOLID_FOREGROUND);
        return result;
    }
}
