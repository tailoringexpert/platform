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
package de.baedorf.tailoringexpert.tailoring;

import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
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

@Log4j2
public class TailoringKatalogSpreadsheetCreator implements DokumentCreator {

    /**
     * {@inheritDoc}
     */
    @Override
    public Datei createDokument(String docId, Tailoring tailoring, Map<String, String> platzhalter) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = createSheet(wb, tailoring);

            if (nonNull(tailoring.getKatalog().getToc())) {
                tailoring.getKatalog().getToc().getKapitel()
                    .forEach(gruppe -> addKapitel(gruppe, sheet));
            }

            range(0, sheet.getRow(0).getPhysicalNumberOfCells())
                .forEach(sheet::autoSizeColumn);


            byte[] content;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                wb.write(os);
                content = os.toByteArray();
            }

            return Datei.builder()
                .docId(docId)
                .type("xlsx")
                .bytes(content)
                .build();
        } catch (Exception e) {
            log.catching(e);
        }
        return null;

    }


    /**
     * Fügt eine Gruppe(Kapitel)  dem Arbeitsblatt hinzu.
     *
     * @param kapitel Die dem Arbeitsblatt hinzuzufügende Gruppe
     * @param sheet   Arbeitsblatt, auf dem die Gruppe hinzugefügt werden soll
     */
    private void addKapitel(Kapitel<TailoringAnforderung> kapitel, Sheet sheet) {
        addRow(sheet, kapitel.getName(), kapitel.getNummer(), "");
        kapitel.getAnforderungen().forEach(
            anforderung -> addRow(sheet, "", anforderung.getPosition(), anforderung.getAusgewaehlt().booleanValue() ? "JA" : "NEIN")
        );

        if (nonNull(kapitel.getKapitel())) {
            kapitel.getKapitel().forEach(
                subgroup -> addKapitel(subgroup, sheet)
            );
        }
    }

    /**
     * Erzeugt ein neues Excel Sheet mit einer Header Zeile.
     *
     * @param wb        Workbook, in der das Sheet erzeugt werden soll
     * @param tailoring Proojektphase mit Kataloginformationen
     * @return Das erzeugte Sheet
     */
    private Sheet createSheet(Workbook wb, Tailoring tailoring) {
        Sheet result = wb.createSheet(tailoring.getName() + "-" + tailoring.getKatalog().getVersion());

        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row row = result.createRow((short) 0);
        row.createCell(0).setCellValue("Bezeichnung");
        row.getCell(0).setCellStyle(headerCellStyle);
        row.createCell(1).setCellValue("Kapitel");
        row.getCell(1).setCellStyle(headerCellStyle);
        row.createCell(2).setCellValue("Anwendbar");
        row.getCell(2).setCellStyle(headerCellStyle);
        result.setAutoFilter(new CellRangeAddress(0, 0, 0, 2));


        return result;
    }

    /**
     * Fügt eine neue Zeile dem Arbeitsblatt hinzu.
     *
     * @param sheet       Arbeitsblatt, auf dem die Zeile hinzugefügt werden soll
     * @param bezeichnung Bezeichnungstext
     * @param position    Position im Kontext (Kapitel und Nummer im Kapitel)
     * @param anwendbar   Text, ob die Zeile (Anforderung) in der Phase anwendbar ist
     */
    private void addRow(Sheet sheet, String bezeichnung, String position, String anwendbar) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(bezeichnung);
        row.createCell(1).setCellValue(position);
        row.createCell(2).setCellValue(anwendbar);
    }
}
