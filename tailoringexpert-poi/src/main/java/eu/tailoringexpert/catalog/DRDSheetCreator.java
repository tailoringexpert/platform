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
import eu.tailoringexpert.domain.DRD;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.IntStream.range;

@Log4j2
public class DRDSheetCreator extends AbstractRequirementsBasedSheetCreator {


    /**
     * Add header to sheet
     *
     * @param sheet sheet to add header to
     */
    void addHeader(Sheet sheet, Styles styles) {

        Row row = sheet.createRow((short) 0);
        row.createCell(0).setCellValue("#");
        row.createCell(1).setCellValue("Title");
        row.createCell(2).setCellValue("Delivery Date");
        row.createCell(3).setCellValue("Action");
        range(0, 4).forEach(i -> row.getCell(i).setCellStyle(styles.getHeaderStyle()));

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 3));
    }

    /**
     * Add chapter to sheet object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param sheet   sheet to add elements to
     */
    void addChapter(Chapter<BaseRequirement> chapter, Sheet sheet, Styles styles) {
        chapter.allRequirements()
            .filter(BaseRequirement::hasDRD)
            .map(BaseRequirement::getDrds)
            .flatMap(Collection::stream)
            .sorted(comparing(DRD::getNumber))
            .collect(Collectors.toCollection(LinkedHashSet::new))
            .forEach(drd -> {
                    Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
                    row.createCell(0).setCellValue(drd.getNumber());
                    row.createCell(1).setCellValue(drd.getTitle());
                    row.createCell(2).setCellValue(drd.getDeliveryDate());
                    row.createCell(3).setCellValue(drd.getAction());

                    range(0, 3).forEach(i -> row.getCell(i).setCellStyle(styles.getDefaultStyle()));
                }
            );
    }

}
