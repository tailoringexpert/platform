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
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Reference;
import eu.tailoringexpert.domain.Requirement;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.range;

/**
 * Creates an Excel sheet of logos contained in basecatalog.
 *
 * @author Michael Bädorf
 */
@Log4j2
public class LogoSheetCreator extends AbstractRequirementsBasedSheetCreator {

    /**
     * Add header to sheet
     *
     * @param sheet sheet to add header to
     */
    @Override
    void addHeader(Sheet sheet, Styles styles) {
        Row row = sheet.createRow((short) 0);
        row.createCell(0).setCellValue("Name");
        row.createCell(1).setCellValue("URL");
        range(0, 2).forEach(i -> row.getCell(i).setCellStyle(styles.getHeaderStyle()));

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 1));
    }

    /**
     * Add chapter to sheet object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param sheet   sheet to add elements to
     */
    @Override
    void addChapter(Chapter<BaseRequirement> chapter, Sheet sheet, Styles styles) {
        chapter.allRequirements().filter(requirement -> nonNull(requirement.getReference())).map(Requirement::getReference).filter(reference -> nonNull(reference.getLogo())).map(Reference::getLogo).sorted(comparing(Logo::getName)).collect(Collectors.toCollection(LinkedHashSet::new)).forEach(logo -> {
            Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(logo.getName());
            row.createCell(1).setCellValue(logo.getUrl());

            range(0, 1).forEach(i -> row.getCell(i).setCellStyle(styles.getDefaultStyle()));
        });
    }

}
