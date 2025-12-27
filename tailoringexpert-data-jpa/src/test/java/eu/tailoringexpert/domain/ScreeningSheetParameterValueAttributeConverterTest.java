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
package eu.tailoringexpert.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@SuppressWarnings({"PMD.AvoidAccessibilityAlteration"})
class ScreeningSheetParameterValueAttributeConverterTest {

    private ScreeningSheetParameterValueAttributeConverter converter;

    @BeforeEach
    void setup() {
        this.converter = new ScreeningSheetParameterValueAttributeConverter();
    }

    @Test
    void convertToDatabaseColumn_ArrayWirdUebergebenStringWirdZurueckgeben() {
        // arrange
        List<Phase> wert = Arrays.asList(A, B);

        // act
        String actual = converter.convertToDatabaseColumn(wert);

        // assert
        assertThat(actual)
            .isNotNull()
            .isEqualTo("[\"A\",\"B\"]");
    }

    @Test
    void convertToDatabaseColumn_StringWirdUebergeben_StringWirdZurueckgeben() {
        // arrange
        String wert = "Ein Beispielwert";

        // act
        String actual = converter.convertToDatabaseColumn(wert);

        // assert
        assertThat(actual)
            .isNotNull()
            .isEqualTo("\"Ein Beispielwert\"");
    }

    @Test
    void convertToDatabaseColumn_ExceptionSimuliert_SneakyThrows() throws Exception {
        // arrange
        String wert = "Ein Beispielwert";

        JsonMapper objectMapperMock = Mockito.mock(JsonMapper.class);
        Field mapperField = converter.getClass().getDeclaredField("mapper");
        mapperField.setAccessible(true);
        mapperField.set(converter, objectMapperMock);

        given(objectMapperMock.writeValueAsString(anyString())).willThrow(new RuntimeException());

        // act
        Throwable actual = catchThrowable(() -> converter.convertToDatabaseColumn(wert));

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void convertToEntityAttribute_StringWirdUebergeben_StringWirdZurueckgeben() {
        // arrange
        String wert = "\"Ein Beispielwert\"";

        // act
        Object actual = converter.convertToEntityAttribute(wert);

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(String.class);
    }

    @Test
    void convertToEntityAttribute_ArrayStringWirdUebergeben_ArraygWirdZurueckgeben() {
        // arrange
        String wert = "[\"A\",\"B\"]";

        // act
        Object actual = converter.convertToEntityAttribute(wert);

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(Collection.class);
    }

    @Test
    void convertToEntityAttribute_ExceptionSimuliert_SneakyThrows() throws Exception {
        // arrange
        String wert = "[\"A\",\"B\"]";

        JsonMapper objectMapperMock = Mockito.mock(JsonMapper.class);
        Field mapperField = converter.getClass().getDeclaredField("mapper");
        mapperField.setAccessible(true);
        mapperField.set(converter, objectMapperMock);

        given(objectMapperMock.readTree(anyString())).willThrow(new RuntimeException());

        // act
        Throwable actual = catchThrowable(() -> converter.convertToEntityAttribute(wert));

        // assert
        assertThat(actual).isNotNull();
    }
}
