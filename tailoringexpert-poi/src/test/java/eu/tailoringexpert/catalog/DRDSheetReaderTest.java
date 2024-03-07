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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

class DRDSheetReaderTest {
    ToDRDMappingFunction reader;

    @BeforeEach
    void setup() {
        this.reader = new ToDRDMappingFunction();
    }

    @Test
    void apply_SheetEntries_MapReturned() throws IOException {
        // arrange

        Sheet sheet;
        try (InputStream is = newInputStream(get("src/test/resources/basecatalog.xlsx"))) {
            assert nonNull(is);
            Workbook wb = WorkbookFactory.create(is);
            sheet = wb.getSheet("DRD");
            assert nonNull(sheet);
            wb.close();
        }

        // act
        Map<String, DRD> actual = reader.apply(sheet);

        // assert
        assertThat(actual)
            .isNotNull()
            .isNotEmpty();
    }
}
