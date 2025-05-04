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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.FileSaver;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;


class RequirementsSheetCreatorTest {

    private ObjectMapper objectMapper;
    private FileSaver fileSaver;
    private Sheet sheet;
    private RequirementSheetCreator creator;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.fileSaver = new FileSaver("target");

        this.sheet = new XSSFWorkbook().createSheet("8.2.1");

        this.creator = new RequirementSheetCreator();
    }

    @Test
    void accept_AllInputsProvided_CatalogDataAddedToSheet() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }

        // act
        creator.accept(catalog, sheet);

        // assert
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            sheet.getWorkbook().write(os);
            fileSaver.accept("requirements.xlsx", os.toByteArray());
        }

        assertThat(sheet.getSheetName()).isEqualTo("8.2.1");
        assertThat(sheet.getLastRowNum()).isPositive();
        assertThat(sheet.getRow(0).getPhysicalNumberOfCells()).isEqualTo(8);
    }
}
