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
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@Log4j2
class ImportRequirementExcelFileReaderTest {

    private TailoringRequirementExcelFileReader excel;

    @BeforeEach
    void setup() {
        this.excel = new TailoringRequirementExcelFileReader();
    }

    @Test
    void apply_ExceptionSimulated_ExceptionThrown() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/leer.xlsx"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        // act
        Map<String, Collection<ImportRequirement>> actual = excel.apply(data);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void apply_InputFileValid_RowsCorrectProcessed() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/TailoringImport.xlsx"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        // act
        Map<String, Collection<ImportRequirement>> actual = excel.apply(data);

        // assert
        actual.entrySet().forEach(entry -> entry.getValue().forEach(log::info));
        assertThat(actual).isNotEmpty();
    }

    @Test
    void apply_WorkbookFactoryException_NullReturned() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/TailoringImport.xlsx"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        // act
        Map<String, Collection<ImportRequirement>> actual;
        try (MockedStatic<WorkbookFactory> wf = Mockito.mockStatic(WorkbookFactory.class)) {
            wf.when(() -> WorkbookFactory.create(any(InputStream.class))).thenThrow(new IOException());
            actual = excel.apply(data);
        }

        // assert
        assertThat(actual).isNull();
    }
}
