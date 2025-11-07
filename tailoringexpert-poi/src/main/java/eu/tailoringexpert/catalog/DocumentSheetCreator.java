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

import eu.tailoringexpert.domain.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Collection;
import java.util.function.Function;

import static java.util.stream.IntStream.range;

/**
 * Creates a Excel sheet of DRDs contained in basecatalog.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class DocumentSheetCreator extends AbstractRequirementsBasedSheetCreator {

    @NonNull
    private Function<Catalog<BaseRequirement>, Collection<Document>> applicableDocumentProvider;

    /**
     * Add header to sheet
     *
     * @param sheet sheet to add header to
     */
    @Override
    void addHeader(Sheet sheet, Styles styles) {

        Row row = sheet.createRow((short) 0);
        row.createCell(0).setCellValue("#");
        row.createCell(1).setCellValue("Title");
        row.createCell(2).setCellValue("Issue");
        row.createCell(3).setCellValue("Revision");
        row.createCell(4).setCellValue("Description");
        range(0, 5).forEach(i -> row.getCell(i).setCellStyle(styles.getHeaderStyle()));

        sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, 4));
    }

    /**
     * Add chapter to sheet object.
     * All subchapter will be evaluated as well.
     *
     * @param toc chapter evaluate
     * @param sheet   sheet to add elements to
     */
    @Override
    void addChapter(Chapter<BaseRequirement> toc, Sheet sheet, Styles styles) {
        applicableDocumentProvider.apply(Catalog.<BaseRequirement>builder().toc(toc).build())
            .forEach(document -> {
                    Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);
                    row.createCell(0).setCellValue(document.getNumber());
                    row.createCell(1).setCellValue(document.getTitle());
                    row.createCell(2).setCellValue(document.getIssue());
                    row.createCell(3).setCellValue(document.getRevision());
                    row.createCell(4).setCellValue(document.getDescription());

                    range(0, 4).forEach(i -> row.getCell(i).setCellStyle(styles.getDefaultStyle()));
                }
            );
    }

}
