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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.TailoringexpertException;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.util.function.Function;

/**
 * Class for creating a basecatalog from a provided Excel file.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class Excel2CatalogConverter implements Function<byte[], Catalog<BaseRequirement>> {

    @NonNull
    Function<Sheet, Chapter<BaseRequirement>> toChapter;

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Catalog<BaseRequirement> apply(byte[] data) {
        log.traceEntry();

        try (ByteArrayInputStream is = new ByteArrayInputStream(data);
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            Chapter<BaseRequirement> root = toChapter.apply(sheet);
            Catalog.CatalogBuilder<BaseRequirement> result = Catalog.<BaseRequirement>builder()
                .version(sheet.getSheetName())
                .toc(root);

            log.traceExit();

            return result.build();

        } catch (Exception e) {
            throw log.throwing(e);
        } finally {
            log.traceExit();
        }
    }

}
