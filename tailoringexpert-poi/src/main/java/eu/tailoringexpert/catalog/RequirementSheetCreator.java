/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2024 Michael Bädorf and others
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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Document;
import eu.tailoringexpert.domain.Phase;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;

/**
 * Creates an Excel sheet of requirements contained in basecatalog.
 *
 * @author Michael Bädorf
 */
@Log4j2
public class RequirementSheetCreator implements BiConsumer<Catalog<BaseRequirement>, Sheet> {

    @Override
    public void accept(Catalog<BaseRequirement> catalog, Sheet sheet) {
        log.traceEntry(() -> catalog, catalog::getVersion);

        Styles styles = new Styles(sheet.getWorkbook());

        catalog.getToc().getChapters()
            .forEach(chapter -> addChapter(chapter, sheet, styles));

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 7));
        range(0, sheet.getRow(0).getPhysicalNumberOfCells())
            .forEach(sheet::autoSizeColumn);

        log.traceExit();
    }

    /**
     * Add chapter to sheet object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param sheet   sheet to add elements to
     */
    private void addChapter(Chapter<BaseRequirement> chapter, Sheet sheet, Styles styles) {
        addRow(chapter.getNumber(), chapter.getName(), sheet, styles);
        chapter.getRequirements().forEach(requirement -> addRow(requirement, sheet, styles));
        chapter.getChapters().forEach(subChapter -> addChapter(subChapter, sheet, styles));

    }

    /**
     * Add a row to provided sheet with provided parameters.
     *
     * @param sheet  sheet to add row to
     * @param number value of cell 0
     * @param name   value of cell 1
     */
    private void addRow(String number, String name, Sheet sheet, Styles styles) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(number);
        row.createCell(1).setCellValue(name);
        row.createCell(2).setCellValue("");
        row.createCell(3).setCellValue("");
        row.createCell(4).setCellValue("");
        row.createCell(5).setCellValue("");
        row.createCell(6).setCellValue("");
        row.createCell(7).setCellValue("");

        range(0, 8).forEach(i -> row.getCell(i).setCellStyle(styles.getHeaderStyle()));
    }

    private void addRow(BaseRequirement requirement, Sheet sheet, Styles styles) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(requirement.getPosition());
        row.createCell(1).setCellValue(requirement.getText());

        row.createCell(2).setCellValue(requirement.getPhases()
            .stream()
            .map(Phase::getValue)
            .collect(joining("\n")));

        row.createCell(3).setCellValue(requirement.getIdentifiers()
            .stream()
            .map(identifier -> identifier.getType() + identifier.getLevel() +
                (nonNull(identifier.getLimitations()) ?
                    identifier.getLimitations()
                        .stream()
                        .map(limitation -> "(" + limitation + ")")
                        .collect(joining()) :
                    "")
            )
            .collect(joining("\n")));

        String reference = "";
        if (nonNull(requirement.getReference())) {
            reference = requirement.getReference().getText();
            if (requirement.getReference().getChanged().booleanValue()) {
                reference = reference + " (mod)";
            }
        }
        row.createCell(4).setCellValue(reference);

        String logo = !reference.isBlank() && nonNull(requirement.getReference().getLogo()) ?
            requirement.getReference().getLogo().getName() :
            "";
        row.createCell(5).setCellValue(logo);


        row.createCell(6).setCellValue(
            nonNull(requirement.getDrds()) ?
                requirement.getDrds()
                    .stream()
                    .map(DRD::getNumber)
                    .collect(joining("\n")) :
                ""
        );

        row.createCell(7).setCellValue(
            nonNull(requirement.getApplicableDocuments()) ?
                requirement.getApplicableDocuments()
                    .stream()
                    .map(Document::getTitle)
                    .collect(joining("\n")) :
                ""
        );

        range(0, 8).forEach(i -> row.getCell(i).setCellStyle(styles.getDefaultStyle()));
    }
}
