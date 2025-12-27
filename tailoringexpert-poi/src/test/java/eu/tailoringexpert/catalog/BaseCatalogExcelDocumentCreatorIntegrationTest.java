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
import eu.tailoringexpert.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;


class BaseCatalogExcelDocumentCreatorIntegrationTest {

    private JsonMapper objectMapper;
    private FileSaver fileSaver;

    private BaseCatalogExcelDocumentCreator creator;

    @BeforeEach
    void setup() {
        this.objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            .build();

        this.fileSaver = new FileSaver("target");

        this.creator = new BaseCatalogExcelDocumentCreator(
            new RequirementSheetCreator(),
            new DRDSheetCreator(),
            new DocumentSheetCreator(new ApplicableDocumentProvider<BaseRequirement>(
                new RequirementAlwaysSelectedPredicate<BaseRequirement>(), new DocumentNumberComparator())),
            new LogoSheetCreator()
        );
    }


    @Test
    void createDocument_validInput_FileCreated() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, BaseRequirement.class)
            );
        }

        Map<String, Object> parameter = new HashMap<>();

        // act
        File actual = creator.createDocument("4711", catalog, parameter);

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("basecatalog.xlsx", actual.getData());
    }
}
