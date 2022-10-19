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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;

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
    public File createDocument(String docId, Tailoring tailoring, Map<String, String> placeholders) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = createSheet(wb, tailoring);

            tailoring.getCatalog().getToc().getChapters()
                .forEach(gruppe -> addChapter(gruppe, sheet));

            range(0, sheet.getRow(0).getPhysicalNumberOfCells())
                .forEach(sheet::autoSizeColumn);

            byte[] content;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                wb.write(os);
                content = os.toByteArray();
            }

            return File.builder()
                .name(docId + ".xlsx")
                .data(content)
                .build();
        } catch (Exception e) {
            log.catching(e);
        }
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
        addRow(sheet, chapter.getName(), chapter.getNumber(), "");
        chapter.getRequirements().forEach(
            requirement -> addRow(sheet, "", requirement.getPosition(), requirement.getSelected().booleanValue() ? "JA" : "NEIN")
        );

        if (nonNull(chapter.getChapters())) {
            chapter.getChapters()
                .forEach(subChapter -> addChapter(subChapter, sheet));
        }
    }

    /**
     * Create sheet in workbook.
     *
     * @param wb workbook to add worksheet
     * @return created worksheet
     */
    private Sheet createSheet(Workbook wb, Tailoring tailoring) {
        Sheet result = wb.createSheet(tailoring.getName() + "-" + tailoring.getCatalog().getVersion());

        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row row = result.createRow((short) 0);
        row.createCell(0).setCellValue("Bezeichnung");
        row.getCell(0).setCellStyle(headerCellStyle);
        row.createCell(1).setCellValue("Chapter");
        row.getCell(1).setCellStyle(headerCellStyle);
        row.createCell(2).setCellValue("Anwendbar");
        row.getCell(2).setCellStyle(headerCellStyle);
        result.setAutoFilter(new CellRangeAddress(0, 0, 0, 2));

        return result;
    }

    /**
     * Add a row to provided sheet with provided parameters.
     *
     * @param sheet      sheet to add row to
     * @param label      value of cell 0
     * @param position   value of cell 1
     * @param applicable value of cell 2
     */
    private void addRow(Sheet sheet, String label, String position, String applicable) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(position);
        row.createCell(2).setCellValue(applicable);
    }
}