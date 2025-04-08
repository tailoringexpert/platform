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
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.TailoringexpertException;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.function.Function;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Log4j2
class Excel2CatalogConverterTest {

    private ObjectMapper objectMapper;
    private FileSaver fileSaver;

    Function<Sheet, Chapter<BaseRequirement>> toChapterMock;
    Excel2CatalogConverter toFunction;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());

        this.objectMapper.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        this.fileSaver = new FileSaver("target");

        this.toChapterMock = mock(Function.class);
        this.toFunction = new Excel2CatalogConverter(
            this.toChapterMock
        );
    }

    @Test
    void apply_MockThrowsException_ExceptionThrown() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.xlsx")) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(toChapterMock.apply(any())).willThrow(new RuntimeException("exception test"));

        // act
        Throwable actual = catchThrowable(() -> toFunction.apply(data));

        // assert
        assertThat(actual)
            .isInstanceOf(RuntimeException.class)
            .hasMessage("exception test");
    }

    @Test
    void apply_AllMocked_FileCreated() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.xlsx")) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(toChapterMock.apply(any())).willReturn(Chapter.<BaseRequirement>builder().build());

        // act
        Catalog<BaseRequirement> actual = toFunction.apply(data);

        // assert
        assertThat(actual).isNotNull();
        verify(toChapterMock).apply(any(Sheet.class));
    }

    @Test
    void apply_NoMocks_FileCreated() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.xlsx")) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        ToChapterFunction toChapter = new ToChapterFunction(
            new ToDRDMappingFunction(),
            new ToLogoMappingFunction(),
            new ToIdentifierFunction(),
            new ToLogoFunction(),
            new ToReferenceFunction(),
            new BuildingChapterConsumer()
        );

        Excel2CatalogConverter noMocksToFunction = new Excel2CatalogConverter(toChapter);


        // act
        Catalog<BaseRequirement> actual = noMocksToFunction.apply(data);

        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }

        // assert
        fileSaver.accept("export.json", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(actual));
        assertThat(actual).isEqualTo(catalog);
    }

    @Test
    void apply_NoMocksChapterWithoutPhases_TailoringExceptionThrown() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog_requirement_without_phase.xlsx")) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        ToChapterFunction toChapter = new ToChapterFunction(
            new ToDRDMappingFunction(),
            new ToLogoMappingFunction(),
            new ToIdentifierFunction(),
            new ToLogoFunction(),
            new ToReferenceFunction(),
            new BuildingChapterConsumer()
        );

        Excel2CatalogConverter noMocksToFunction = new Excel2CatalogConverter(toChapter);


        // act
        Throwable actual = catchThrowable(() -> noMocksToFunction.apply(data));

        // assert
        assertThat(actual)
            .isInstanceOf(TailoringexpertException.class)
            .hasMessage("Could not convert worksheet chapter row 3");
    }
}
