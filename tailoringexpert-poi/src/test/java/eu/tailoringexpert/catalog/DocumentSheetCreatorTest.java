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

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.domain.ApplicableDocumentProvider;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.DocumentNumberComparator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;


class DocumentSheetCreatorTest {

    private JsonMapper objectMapper;
    private FileSaver fileSaver;
    private Sheet sheet;
    private DocumentSheetCreator creator;

    @BeforeEach
    void setup() {
        this.objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            .withConfigOverride(List.class, cfg ->
                cfg.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY))
            )
            .build();
        this.fileSaver = new FileSaver("target");

        this.sheet = new XSSFWorkbook().createSheet("DOCUMENT");

        this.creator = new DocumentSheetCreator(
            new ApplicableDocumentProvider<BaseRequirement>(new RequirementAlwaysSelectedPredicate<BaseRequirement>(), new DocumentNumberComparator())
        );
    }

    @Test
    void accept_AllInputsProvided_FileCreated() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/catalog_8.2.2.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, BaseRequirement.class)
            );
        }


        // act
        creator.accept(catalog, sheet);

        // assert
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            sheet.getWorkbook().write(os);
            fileSaver.accept("document.xlsx", os.toByteArray());
        }

        assertThat(sheet.getSheetName()).isEqualTo("DOCUMENT");
        assertThat(sheet.getLastRowNum()).isPositive();
        assertThat(sheet.getRow(0).getPhysicalNumberOfCells()).isEqualTo(5);
    }
}
