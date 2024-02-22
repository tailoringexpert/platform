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
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;

import eu.tailoringexpert.FileSaver;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


class BaseCatalogExcelDocumentCreatorTest {

    private ObjectMapper objectMapper;
    private FileSaver fileSaver;

    private BiConsumer<Catalog<BaseRequirement>, Sheet> requirementSheetCreatorMock;
    private BiConsumer<Catalog<BaseRequirement>, Sheet> drdSheetCreator;
    private BiConsumer<Catalog<BaseRequirement>, Sheet> logoSheetCreator;
    private BaseCatalogExcelDocumentCreator creator;

    @BeforeEach
    void setup() throws URISyntaxException {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        MutableConfigOverride override = this.objectMapper.configOverride(List.class);
        override.setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));

        this.objectMapper.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        this.fileSaver = new FileSaver("target");

        this.requirementSheetCreatorMock = mock(BiConsumer.class);
        this.drdSheetCreator = mock(BiConsumer.class);
        this.logoSheetCreator = mock(BiConsumer.class);

        this.creator = new BaseCatalogExcelDocumentCreator(
            this.requirementSheetCreatorMock,
            this.drdSheetCreator,
            this.logoSheetCreator
        );
    }

    @Test
    void createDocument_MockedSheetException_NullReturned() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }

        doThrow(new RuntimeException())
            .when(requirementSheetCreatorMock).accept(eq(catalog), any(Sheet.class));

        Map<String, Object> parameter = new HashMap<>();

        // act
        File actual = creator.createDocument("4711", catalog, parameter);

        // assert
        assertThat(actual).isNull();
        verify(requirementSheetCreatorMock, times(1)).accept(eq(catalog), any(Sheet.class));
        verify(drdSheetCreator, times(0)).accept(eq(catalog), any(Sheet.class));
        verify(logoSheetCreator, times(0)).accept(eq(catalog), any(Sheet.class));
    }

    @Test
    void createDocument_MockedSheetCreators_FileCreated() throws IOException {
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
        verify(requirementSheetCreatorMock, times(1)).accept(eq(catalog), any(Sheet.class));
        verify(drdSheetCreator, times(1)).accept(eq(catalog), any(Sheet.class));
        verify(logoSheetCreator, times(1)).accept(eq(catalog), any(Sheet.class));
    }
}
