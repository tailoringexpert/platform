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
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.function.BiConsumer;

import static java.util.stream.IntStream.range;

@Log4j2
public abstract class AbstractRequirementsBasedSheetCreator implements BiConsumer<Catalog<BaseRequirement>, Sheet> {

    @Override
    public void accept(Catalog<BaseRequirement> catalog, Sheet sheet) {
        log.traceEntry(() -> catalog, catalog::getVersion);

        Styles styles = new Styles(sheet.getWorkbook());
        addHeader(sheet, styles);
        addChapter(catalog.getToc(), sheet, styles);

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, sheet.getRow(0).getPhysicalNumberOfCells()-1));

        range(0, sheet.getRow(0).getPhysicalNumberOfCells()-1)
            .forEach(sheet::autoSizeColumn);

        log.traceExit();
    }

    /**
     * Add header to sheet
     *
     * @param sheet sheet to add header to
     */
    abstract void addHeader(Sheet sheet, Styles styles);

    /**
     * Add chapter to sheet object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param sheet   sheet to add elements to
     */
    abstract void addChapter(Chapter<BaseRequirement> chapter, Sheet sheet, Styles styles);


}
