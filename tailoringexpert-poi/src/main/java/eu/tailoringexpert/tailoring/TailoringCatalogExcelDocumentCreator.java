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
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.StreamSupport;

import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.of;

/**
 * Create Excel requirement catalog file.
 *
 * @author Michael Bädorf
 */
@Log4j2
public class TailoringCatalogExcelDocumentCreator implements DocumentCreator {

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(String docId, Tailoring tailoring, Map<String, Object> placeholders) {
        log.traceEntry(() -> docId);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = createSheet(wb, tailoring);

            tailoring.getCatalog().getToc().getChapters().forEach(gruppe -> addChapter(gruppe, sheet));

            applyTextStyle(sheet);
            Arrays.stream(new int[]{1, 2, 4, 5}).forEach(sheet::autoSizeColumn);

            copySheet(wb, 0);
            deleteColumnsNotUsedForImportSheet(wb.getSheetAt(0));

            byte[] content;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                wb.write(os);
                content = os.toByteArray();
            }

            File result = File.builder().name(docId + ".xlsx").data(content).build();
            log.traceExit();
            return result;
        } catch (Exception e) {
            log.catching(e);
        }
        log.traceExit();
        return null;
    }

    /**
     * Add chapter to sheet object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param sheet   sheet to add elements to
     */
    private void addChapter(Chapter<TailoringRequirement> chapter, Sheet sheet) {
        addRow(sheet, chapter.getName(), chapter.getNumber());
        chapter.getRequirements().forEach(requirement -> addRow(sheet, requirement));

        chapter.getChapters().forEach(subChapter -> addChapter(subChapter, sheet));
    }

    /**
     * Create sheet in workbook.
     *
     * @param wb workbook to add worksheet
     * @return created worksheet
     */
    private Sheet createSheet(Workbook wb, Tailoring tailoring) {
        Sheet result = wb.createSheet(tailoring.getName() + "-" + tailoring.getCatalog().getVersion() + "-IMPORT");

        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.TOP);

        Row row = result.createRow((short) 0);
        row.createCell(0).setCellValue("Label");
        row.getCell(0).setCellStyle(headerCellStyle);

        row.createCell(1).setCellValue("Chapter");
        row.getCell(1).setCellStyle(headerCellStyle);
        row.createCell(2).setCellValue("Applicable");
        row.getCell(2).setCellStyle(headerCellStyle);
        row.createCell(3).setCellValue("Text");
        row.getCell(3).setCellStyle(headerCellStyle);

        row.createCell(4).setCellValue("Selection changed");
        row.getCell(4).setCellStyle(headerCellStyle);
        row.createCell(5).setCellValue("Text changed");
        row.getCell(5).setCellStyle(headerCellStyle);

        result.setColumnWidth(0, 40 * 256);
        result.setColumnWidth(3, 60 * 256);
        result.setAutoFilter(new CellRangeAddress(0, 0, 0, 5));

        return result;
    }

    /**
     * Add a row to provided sheet with provided parameters.
     *
     * @param sheet    sheet to add row to
     * @param label    value of cell 0
     * @param position value of cell 1
     */
    private void addRow(Sheet sheet, String label, String position) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(position);
    }

    /**
     * Add a row to provided sheet with provided parameters.
     *
     * @param sheet       sheet to add row to
     * @param requirement tailoring requirement to be displayed in row
     */
    private void addRow(Sheet sheet, TailoringRequirement requirement) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue("");
        row.createCell(1).setCellValue(requirement.getPosition());
        row.createCell(2).setCellValue(requirement.getSelected().booleanValue() ? "JA" : "NEIN");
        row.createCell(3).setCellValue(requirement.getText());
        row.createCell(4).setCellValue(nonNull(requirement.getSelectionChanged()));
        row.createCell(5).setCellValue(nonNull(requirement.getTextChanged()));
    }


    /**
     * Copies the sheet with the provided index.
     *
     * @param wb    workbook containg sheet to clone
     * @param index index of sheet to clone
     */
    private void copySheet(Workbook wb, int index) {
        wb.cloneSheet(index);
        String baseName = wb.getSheetName(index);
        String name = baseName.substring(0, baseName.lastIndexOf('-')) + "-EXPORT";
        wb.setSheetName(wb.getNumberOfSheets() - 1, name);
    }

    /**
     * Deletes columns in sheet which are not used as input for import.
     *
     * @param sheet sheet to delete text
     */
    private void deleteColumnsNotUsedForImportSheet(Sheet sheet) {
        Row header = sheet.getRow(0);
        of(4, 5).forEach(index -> header.removeCell(header.getCell(index)));

        StreamSupport.stream(sheet.spliterator(), false)
            .skip(1)
            .filter(row -> nonNull(row.getCell(3)))
            .forEach(row -> of(3, 4, 5).forEach(index -> row.removeCell(row.getCell(index))));
        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 3));
    }

    void applyTextStyle(Sheet sheet) {
        CellStyle wrapStyle = sheet.getWorkbook().createCellStyle();
        wrapStyle.setWrapText(true);
        wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);
        StreamSupport.stream(sheet.spliterator(), false)
            .skip(1)
            .filter(row -> nonNull(row.getCell(3)))
            .forEach(row -> row.getCell(3).setCellStyle(wrapStyle));
    }
}
