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
package eu.tailoringexpert.tailoring;

import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK;

@Log4j2
public class TailoringRequirementExcelFileReader implements Function<byte[], Map<String, Collection<ImportRequirement>>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Collection<ImportRequirement>> apply(byte[] data) {
        Map<String, Collection<ImportRequirement>> result = new HashMap<>();

        try (ByteArrayInputStream is = new ByteArrayInputStream(data);
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() == 0) {
                return emptyMap();
            }

            Iterator<Row> rowIterator = sheet.iterator();
            // header überlesen
            rowIterator.next();

            AtomicReference<String> chapter = new AtomicReference<>();

            while (rowIterator.hasNext()) {
                Row requirement = rowIterator.next();
                String label = requirement.getCell(0, CREATE_NULL_AS_BLANK).getStringCellValue();
                String position = requirement.getCell(1, CREATE_NULL_AS_BLANK).getStringCellValue();
                String applicable = requirement.getCell(2, CREATE_NULL_AS_BLANK).getStringCellValue();
                String text = requirement.getCell(3, CREATE_NULL_AS_BLANK).getStringCellValue();


                if (!label.isEmpty()) {
                    chapter.set(position);
                    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") final Collection<ImportRequirement> chapterRequirements = new ArrayList<>();
                    result.put(chapter.get(), chapterRequirements);
                } else {
                    result.get(chapter.get()).add(ImportRequirement.builder()
                        .label(label)
                        .position(position)
                        .applicable(applicable)
                        .text(text)
                        .build());
                }
            }
            return result;
        } catch (Exception e) {
            log.catching(e);
        }
        return emptyMap();
    }
}
