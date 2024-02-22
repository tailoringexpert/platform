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
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.File.FileBuilder;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.function.BiConsumer;

import static eu.tailoringexpert.domain.File.builder;

@Log4j2
@AllArgsConstructor
public class BaseCatalogExcelDocumentCreator implements DocumentCreator {

    @NonNull
    BiConsumer<Catalog<BaseRequirement>, Sheet> requirementSheetCreator;
    @NonNull
    BiConsumer<Catalog<BaseRequirement>, Sheet> drdSheetCreator;
    @NonNull
    BiConsumer<Catalog<BaseRequirement>, Sheet> logoSheetCreator;


    @Override
    public File createDocument(String docId, Catalog<BaseRequirement> catalog, Map<String, Object> placeholders) {
        log.traceEntry(() -> docId, () -> catalog.getVersion(), () -> placeholders);

        FileBuilder builder = builder().name(docId + ".xlsx");
        try (Workbook wb = new XSSFWorkbook()) {

            requirementSheetCreator.accept(catalog, wb.createSheet(catalog.getVersion()));
            drdSheetCreator.accept(catalog, wb.createSheet("DRD"));
            logoSheetCreator.accept(catalog, wb.createSheet("LOGO"));

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                wb.write(os);
                builder.data(os.toByteArray());
            }

            File result = builder.build();
            log.traceExit();
            return result;
        } catch (Exception e) {
            log.throwing(e);
        }
        log.traceExit();
        return null;
    }

}
