/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Document;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;

@Log4j2
public class ApplicableDocumentSheetCreator extends AbstractRequirementsBasedSheetCreator {

    @Override
    void addChapter(Chapter<BaseRequirement> chapter, Sheet sheet, Styles styles) {
        Map<Document, Collection<BaseRequirement>> document2Requirements = new HashMap<>();
        chapter.getChapters()
            .forEach(subChapter -> collectDocumentRequirements(subChapter, document2Requirements));

        document2Requirements.forEach((document, requirements) ->
            requirements.forEach(requirement -> addRow(document, requirement, sheet, styles))
        );
    }

    /**
     * Collects and aggregates applicable documents.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param document2Requirements to add documents to
     */
    private void collectDocumentRequirements(Chapter<BaseRequirement> chapter, Map<Document, Collection<BaseRequirement>> document2Requirements) {
        chapter.getRequirements()
            .stream()
            .filter(requirement -> nonNull(requirement.getApplicableDocuments()))
            .forEach(requirement ->
                requirement.getApplicableDocuments().forEach(document -> {
                    Collection<BaseRequirement> requirements = document2Requirements.get(document);
                    if (isNull(requirements)) {
                        requirements = new ArrayList<>();
                        document2Requirements.put(document, requirements);
                    }

                    requirements.add(BaseRequirement.builder()
                        .position(chapter.getNumber() + "." + requirement.getPosition())
                        .text(requirement.getText())
                        .build());
                })
            );
        chapter.getChapters().forEach(subChapter -> collectDocumentRequirements(subChapter, document2Requirements));
    }


    /**
     * Add a row to provided sheet with provided parameters.
     *
     * @param sheet sheet to add row to
     */
    protected void addHeader(Sheet sheet, Styles styles) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue("#");
        row.createCell(1).setCellValue("Document");
        row.createCell(2).setCellValue("Position");
        row.createCell(3).setCellValue("Requirement");

        range(0, 4).forEach(i -> row.getCell(i).setCellStyle(styles.getHeaderStyle()));
    }


    /**
     * Add a row to provided sheet with provided parameters.
     *
     * @param sheet sheet to add row to
     */
    private void addRow(Document document, BaseRequirement requirement, Sheet sheet, Styles styles) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(document.getNumber());
        row.createCell(1).setCellValue(document.getTitle());
        row.createCell(2).setCellValue(requirement.getPosition());
        row.createCell(3).setCellValue(requirement.getText());
        range(0, 4).forEach(i -> row.getCell(i).setCellStyle(styles.getDefaultStyle()));

    }


}
