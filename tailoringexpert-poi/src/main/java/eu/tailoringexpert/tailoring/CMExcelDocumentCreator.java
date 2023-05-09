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
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import static eu.tailoringexpert.domain.File.FileBuilder;
import static eu.tailoringexpert.domain.File.builder;
import static java.nio.file.Files.newInputStream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;
import static org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT;

/**
 * Create Excel Compliance Matrix file. <p>
 * Only chapters will be considered while generating the file.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class CMExcelDocumentCreator implements DocumentCreator {
    @NonNull
    private RendererRequestConfigurationSupplier requestConfigurationSupplier;

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
                               Map<String, Object> placeholders) {
        log.traceEntry(() -> docId, () -> tailoring.getCatalog().getVersion(), () -> placeholders);

        try {
            FileBuilder builder = builder().name(docId + ".xlsx");
            RendererRequestConfiguration configuration = requestConfigurationSupplier.get();
            java.io.File template = Paths.get(configuration.getTemplateHome() + "/" + tailoring.getCatalog().getVersion() + "/cm.xlsx").toFile();
            try (Workbook wb = new XSSFWorkbook(newInputStream(template.toPath()))) {
                Sheet cmSheet = createCMSheet(wb);

                Catalog<TailoringRequirement> catalog = tailoring.getCatalog();
                catalog.getToc().getChapters()
                    .forEach(chapter -> addChapter(chapter, 1, cmSheet, placeholders));

                Collection<DRDElement> drds = new LinkedList<>();
                addDRD(catalog.getToc(), drds, tailoring.getPhases());

                range(0, cmSheet.getRow(0).getPhysicalNumberOfCells())
                    .forEach(cmSheet::autoSizeColumn);

                Sheet drdSheet = createDRDSheet(wb, drds);
                range(0, drdSheet.getRow(0).getPhysicalNumberOfCells())
                    .forEach(drdSheet::autoSizeColumn);

                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    wb.write(os);
                    builder.data(os.toByteArray());
                }
            }

            File result = builder.build();
            log.traceExit();
            return result;
        } catch (Exception e) {
            log.throwing(e);
        }
        log.traceExit();
        return null;
    }

    /**
     * Evaluate all applicable DRD in chapter for given phases and add them to row object.
     *
     * @param chapter chapter to retrieve requirements DRDs of
     * @param rows    object to add DRDs to
     * @param phases  phase of tailoring to use of applicabilty check
     */
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

    /**
     * Create sheet DRD in workbook containing all referenced DRDs.
     *
     * @param wb   workbook to add worksheet "DRD"
     * @param drds DRD to add to worksheet
     * @return created worksheet
     */
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

    /**
     * Add row to worksheet.
     *
     * @param sheet sheet to add to
     * @param drd   drd to add to sheet
     */
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

    /**
     * Add chapter to sheet object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param level   chapter level
     * @param sheet   sheet to add elements to
     */
    private void addChapter(Chapter<TailoringRequirement> chapter,
                            int level,
                            Sheet sheet,
                            Map<String, Object> placeholders) {
        addRow(sheet, level, chapter.getNumber(), chapter.getName(), placeholders);
        AtomicInteger nextLevel = new AtomicInteger(level + 1);
        chapter.getChapters()
            .forEach(subChapter -> addChapter(subChapter, nextLevel.get(), sheet, placeholders));
        addRequirements(chapter.getRequirements(), nextLevel.get(), sheet, placeholders);
    }

    /**
     * Hook fpr adding also requirements to CM instead of only adding chapters.<p>
     * The hook itself doesn't add requirements to CM.
     *
     * @param requirements requirements to add
     * @param level        level of requirements
     * @param sheet        sheet to add to
     * @param placeholders placeholder to use
     */
    protected void addRequirements(Collection<TailoringRequirement> requirements,
                                   int level,
                                   Sheet sheet,
                                   Map<String, Object> placeholders) {
        // hook for adding also requirements to cm instead of "only" chapters
    }

    /**
     * Create sheet CM in workbook.
     *
     * @param wb workbook to add worksheet "CM"
     * @return created worksheet
     */
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

    /**
     * Add chapter row to provided sheet.
     *
     * @param sheet   sheet to add row to
     * @param level   chapter hierarchy
     * @param chapter number of chapter
     * @param title   title of chapter
     */
    protected void addRow(Sheet sheet,
                          int level,
                          String chapter,
                          String title,
                          Map<String, Object> placeholders) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);

        CellStyle cellStyle;
        if (MAIN_CHAPTER == level) {
            cellStyle = createCellStyle(sheet, IndexedColors.LIGHT_BLUE);
        } else if (SUB_CHAPTER == level) {
            cellStyle = createCellStyle(sheet, IndexedColors.LIGHT_GREEN);
        } else {
            cellStyle = sheet.getWorkbook().createCellStyle();
        }

        row.createCell(0).setCellValue(text(chapter, placeholders));
        row.getCell(0).setCellStyle(cellStyle);

        row.createCell(1).setCellValue(text(title, placeholders));
        row.getCell(1).setCellStyle(cellStyle);

        row.createCell(2).setCellStyle(cellStyle);
        row.createCell(3).setCellStyle(cellStyle);
        row.createCell(4).setCellStyle(cellStyle);

    }

    /**
     * Create cell in sheet with given color as fill foreground.
     *
     * @param sheet sheet to create cell in
     * @param color color to use as fill foreground color
     * @return created cell
     */
    private CellStyle createCellStyle(Sheet sheet, IndexedColors color) {
        CellStyle result = sheet.getWorkbook().createCellStyle();
        result.setFillForegroundColor(color.index);
        result.setFillPattern(SOLID_FOREGROUND);
        return result;
    }

    /**
     * Replaces placeholders and removes all html tags to provide a plain text.
     *
     * @param text         text to modify
     * @param placeholders placeholder for substituation
     * @return plain text string with replaces placeholders
     */
    private String text(String text, Map<String, Object> placeholders) {
        AtomicReference<String> updatedText = new AtomicReference<>(text);
        placeholders.entrySet()
            .forEach(entry -> updatedText.set(updatedText.get().replace(
                entry.getKey(),
                nonNull(entry.getValue()) ? entry.getValue().toString() : entry.getKey())
            ));
        Document document = Jsoup.parseBodyFragment(updatedText.get());
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document.body().text();
    }
}
