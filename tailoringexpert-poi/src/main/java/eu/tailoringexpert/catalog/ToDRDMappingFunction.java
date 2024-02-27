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

import eu.tailoringexpert.domain.DRD;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

/**
 * Class for creating a map containing number to corresponding DRD.
 *
 * @author Michael Bädorf
 */
public class ToDRDMappingFunction implements Function<Sheet, Map<String, DRD>> {

    /**
     * Reads DRD sheet data and creates a map with its values.
     *
     * @param sheet sheet to read DRD data from
     * @return Map containing number -> DRD mapping
     */
    @Override
    public Map<String, DRD> apply(Sheet sheet) {
        Map<String, DRD> result = new HashMap<>();
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String number = row.getCell(0, CREATE_NULL_AS_BLANK).getStringCellValue().trim();
            result.put(number, DRD.builder()
                .number(number)
                .title(row.getCell(1, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .deliveryDate(row.getCell(2, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .action(row.getCell(3, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .build()
            );
        }
        return result;
    }
}
