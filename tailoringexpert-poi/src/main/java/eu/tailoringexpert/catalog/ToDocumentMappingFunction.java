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

import eu.tailoringexpert.domain.Document;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

/**
 * Class for creating a map containing number to corresponding Document.
 *
 * @author Michael Bädorf
 */
public class ToDocumentMappingFunction implements Function<Sheet, Map<String, Document>> {

    /**
     * Reads DRD sheet data and creates a map with its values.
     *
     * @param sheet sheet to read DRD data from
     * @return Map containing number -> DRD mapping
     */
    @Override
    public Map<String, Document> apply(Sheet sheet) {
        Map<String, Document> result = new HashMap<>();
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            String title = row.getCell(1, CREATE_NULL_AS_BLANK).getStringCellValue().trim();
            result.put(title, Document.builder()
                .number(row.getCell(0, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .title(row.getCell(1, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .issue(row.getCell(2, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .revision(row.getCell(3, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .description(row.getCell(4, CREATE_NULL_AS_BLANK).getStringCellValue().trim())
                .build()
            );
        }
        return result;
    }
}
