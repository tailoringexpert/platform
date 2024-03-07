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

import eu.tailoringexpert.domain.Logo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

public class ToLogoMappingFunction implements Function<Sheet, Map<String, Logo>> {

    @Override
    public Map<String, Logo> apply(Sheet sheet) {
        Map<String, Logo> result = new HashMap<>();
        Iterator<Row> logoRowIterator = sheet.iterator();
        logoRowIterator.next();
        while (logoRowIterator.hasNext()) {
            Row row = logoRowIterator.next();
            String name = row.getCell(0, CREATE_NULL_AS_BLANK).getStringCellValue().trim();
            result.put(name, Logo.builder()
                .name(name)
                .url(row.getCell(1, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .build()
            );
        }
        return result;
    }
}
