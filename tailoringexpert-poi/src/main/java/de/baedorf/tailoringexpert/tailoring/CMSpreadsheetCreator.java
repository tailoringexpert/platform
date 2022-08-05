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

import de.baedorf.tailoringexpert.domain.DRD;
import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.Phase;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
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
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static de.baedorf.tailoringexpert.domain.Datei.*;
import static java.nio.file.Files.newInputStream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;
import static org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT;

@Log4j2
@RequiredArgsConstructor
public class CMSpreadsheetCreator implements DokumentCreator {

    @NonNull
    private Function<String, File> templateSupplier;

    @NonNull
    private BiFunction<Kapitel<TailoringAnforderung>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    private static final int MAIN_CHAPTER = 1;
    private static final int SUB_CHAPTER = 2;

    /**
     * {@inheritDoc}
     */
    @Override
    public Datei createDokument(String docId,
                                Tailoring tailoring,
                                Map<String, String> platzhalter) {

        try {
            DateiBuilder result = builder()
                .docId(docId)
                .type("xlsx");

            File template = templateSupplier.apply(tailoring.getKatalog().getVersion() + "/cm.xlsx");
            try (Workbook wb = new XSSFWorkbook(newInputStream(template.toPath()))) {
                Sheet cmSheet = createCMSheet(wb);

                Collection<DRDElement> drds = new LinkedList<>();
                Katalog<TailoringAnforderung> katalog = tailoring.getKatalog();
                katalog.getToc().getKapitel()
                    .forEach(kapitel -> addKapitel(kapitel, 1, cmSheet));
                addDRD(katalog.getToc(), drds, tailoring.getPhasen());

                range(0, cmSheet.getRow(0).getPhysicalNumberOfCells())
                    .forEach(cmSheet::autoSizeColumn);

                Sheet drdSheet = createDRDSheet(wb, drds);
                range(0, drdSheet.getRow(0).getPhysicalNumberOfCells())
                    .forEach(drdSheet::autoSizeColumn);

                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    wb.write(os);
                    result.bytes(os.toByteArray());
                }
            }
            return result.build();
        } catch (Exception e) {
            log.catching(e);
        }
        return null;

    }

    void addDRD(@NonNull Kapitel<TailoringAnforderung> gruppe, Collection<DRDElement> zeilen, Collection<Phase> phasen) {
        drdProvider.apply(gruppe, phasen)
            .entrySet()
            .forEach(entry -> zeilen.add(DRDElement.builder()
                .titel(entry.getKey().getTitel())
                .datum(entry.getKey().getLieferzeitpunkt())
                .anforderung(entry.getValue())
                .nummer(entry.getKey().getNummer())
                .aktion(entry.getKey().getAktion())
                .build()));
    }


    /**
     * Erzeugt ein neues Excel Sheet mit einer Header Zeile.
     *
     * @param wb Workbook, in der das Sheet erzeugt werden soll
     * @return Das erzeugte Sheet
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

    private void addRow(Sheet sheet, DRDElement drd) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);

        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setWrapText(true);

        row.createCell(0).setCellValue(drd.getTitel());
        row.getCell(0).setCellStyle(cellStyle);

        row.createCell(1).setCellValue(drd.getDatum());
        row.getCell(1).setCellStyle(cellStyle);

        row.createCell(2).setCellValue(drd.getAnforderung().stream().collect(joining(", \n")));
        row.getCell(2).setCellStyle(cellStyle);

        row.createCell(3).setCellValue(drd.getNummer());
        row.getCell(3).setCellStyle(cellStyle);

        row.createCell(4).setCellValue(drd.getAktion());
        row.getCell(4).setCellStyle(cellStyle);


    }

    private void addKapitel(Kapitel<TailoringAnforderung> kapitel, int ebene, Sheet sheet) {
        addRow(sheet, ebene, kapitel.getNummer(), kapitel.getName());
        if (nonNull(kapitel.getKapitel())) {
            AtomicInteger naechsteEbene = new AtomicInteger(ebene + 1);
            kapitel.getKapitel()
                .forEach(subgroup -> addKapitel(subgroup, naechsteEbene.get(), sheet));
        }
    }

    /**
     * Erzeugt ein neues Excel Sheet mit einer Header Zeile.
     *
     * @param wb Workbook, in der das Sheet erzeugt werden soll
     * @return Das erzeugte Sheet
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
     * Fügt eine neue Zeile dem Arbeitsblatt hinzu.
     *
     * @param sheet   Arbeitsblatt, auf dem die Zeile hinzugefügt werden soll
     * @param kapitel Bezeichnungstext
     * @param titel   Position im Kontext (Kapitel und Nummer im Kapitel)
     */
    private void addRow(Sheet sheet, int ebene, String kapitel, String titel) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);

        CellStyle cellStyle;
        if (MAIN_CHAPTER == ebene) {
            cellStyle = createCellStyle(sheet, IndexedColors.LIGHT_BLUE);
        } else if (SUB_CHAPTER == ebene) {
            cellStyle = createCellStyle(sheet, IndexedColors.LIGHT_GREEN);
        } else {
            cellStyle = sheet.getWorkbook().createCellStyle();
        }

        row.createCell(0).setCellValue(kapitel);
        row.getCell(0).setCellStyle(cellStyle);

        row.createCell(1).setCellValue(titel);
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
