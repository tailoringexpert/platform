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
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
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
import static tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;


class BaseCatalogExcelDocumentCreatorTest {

    private JsonMapper objectMapper;

    private BiConsumer<Catalog<BaseRequirement>, Sheet> requirementSheetCreatorMock;
    private BiConsumer<Catalog<BaseRequirement>, Sheet> drdSheetCreator;
    private BiConsumer<Catalog<BaseRequirement>, Sheet> documentSheetCreator;
    private BiConsumer<Catalog<BaseRequirement>, Sheet> logoSheetCreator;
    private BaseCatalogExcelDocumentCreator creator;

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

        this.requirementSheetCreatorMock = mock(BiConsumer.class);
        this.drdSheetCreator = mock(BiConsumer.class);
        this.documentSheetCreator = mock(BiConsumer.class);
        this.logoSheetCreator = mock(BiConsumer.class);

        this.creator = new BaseCatalogExcelDocumentCreator(
            this.requirementSheetCreatorMock,
            this.drdSheetCreator,
            this.documentSheetCreator,
            this.logoSheetCreator
        );
    }

    @Test
    void createDocument_MockedSheetException_NullReturned() throws IOException {
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

        doThrow(new RuntimeException())
            .when(requirementSheetCreatorMock).accept(eq(catalog), any(Sheet.class));

        Map<String, Object> parameter = new HashMap<>();

        // act
        File actual = creator.createDocument("4711", catalog, parameter);

        // assert
        assertThat(actual).isNull();
        verify(requirementSheetCreatorMock, times(1)).accept(eq(catalog), any(Sheet.class));
        verify(drdSheetCreator, times(0)).accept(eq(catalog), any(Sheet.class));
        verify(documentSheetCreator, times(0)).accept(eq(catalog), any(Sheet.class));
        verify(logoSheetCreator, times(0)).accept(eq(catalog), any(Sheet.class));
    }

    @Test
    void createDocument_MockedSheetCreators_FileCreated() throws IOException {
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
        verify(requirementSheetCreatorMock, times(1)).accept(eq(catalog), any(Sheet.class));
        verify(drdSheetCreator, times(1)).accept(eq(catalog), any(Sheet.class));
        verify(documentSheetCreator, times(1)).accept(eq(catalog), any(Sheet.class));
        verify(logoSheetCreator, times(1)).accept(eq(catalog), any(Sheet.class));
    }
}
