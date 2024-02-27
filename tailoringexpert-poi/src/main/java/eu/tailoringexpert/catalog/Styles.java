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

import lombok.Getter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import static org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND;
import static org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT;
import static org.apache.poi.ss.usermodel.VerticalAlignment.TOP;

/**
 * Styles to be used for creating Excel export file.
 *
 * @author Michael Bädorf
 */
public class Styles {

    @Getter
    CellStyle headerStyle;
    @Getter
    CellStyle defaultStyle;

    public Styles(Workbook wb) {
        this.defaultStyle = wb.createCellStyle();
        this.defaultStyle.setVerticalAlignment(TOP);
        this.defaultStyle.setWrapText(true);

        this.headerStyle = wb.createCellStyle();
        this.headerStyle.setFillForegroundColor(GREY_25_PERCENT.index);
        this.headerStyle.setFillPattern(SOLID_FOREGROUND);
        this.headerStyle.setVerticalAlignment(TOP);
    }
}
