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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MutableConfigOverride;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.domain.ApplicableDocumentProvider;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.DocumentNumberComparator;
import eu.tailoringexpert.domain.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;


class BaseCatalogExcelDocumentCreatorIntegrationTest {

    private ObjectMapper objectMapper;
    private FileSaver fileSaver;

    private BaseCatalogExcelDocumentCreator creator;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        MutableConfigOverride override = this.objectMapper.configOverride(List.class);
        override.setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

        this.objectMapper.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        this.fileSaver = new FileSaver("target");

        this.creator  = new BaseCatalogExcelDocumentCreator(
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
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }

        Map<String, Object> parameter = new HashMap<>();

        // act
        File actual = creator.createDocument("4711", catalog, parameter);

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("basecatalog.xlsx", actual.getData());
    }
}
