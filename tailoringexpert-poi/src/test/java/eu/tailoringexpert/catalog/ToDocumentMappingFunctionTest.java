/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2025 Michael Bädorf and others
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
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static java.util.Objects.nonNull;


@Log4j2
class ToDocumentMappingFunctionTest {
    ToDocumentMappingFunction toFunction;

    @BeforeEach
    void setup() {
        this.toFunction = new ToDocumentMappingFunction();
    }

    @Test
    void apply_NoMocksChapterWithoutPhases_TailoringExceptionThrown() throws Exception {
        // arrange
        Workbook wb = null;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.xlsx")) {
            assert nonNull(is);
            wb = WorkbookFactory.create(is);
        }


        // act
        Map<String, Document> actual = toFunction.apply(wb.getSheet("AD"));

        // assert
        log.debug(actual);

    }
}
