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
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


@Log4j2
class Excel2CatalogConverterIntegrationTest {

    private ObjectMapper objectMapper;
    private FileSaver fileSaver;

    Excel2CatalogConverter toFunction;

    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);

        this.fileSaver = new FileSaver("target");

        ToChapterFunction toChapterFunction =  new ToChapterFunction(
            new ToDRDMappingFunction(),
            new ToLogoMappingFunction(),
            new ToDocumentMappingFunction(),
            new ToIdentifierFunction(),
            new ToLogoFunction(),
            new ToReferenceFunction(),
            new BuildingChapterConsumer()
        );

        this.toFunction = new Excel2CatalogConverter(
            toChapterFunction
        );


    }

    @Test
    void apply_validInput_FileCreated() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.xlsx")) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        // act
        Catalog<BaseRequirement> actual = toFunction.apply(data);

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
    void apply_chapterWithoutPhases_TailoringExceptionThrown() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog_requirement_without_phase.xlsx")) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        // act
        Throwable actual = catchThrowable(() -> toFunction.apply(data));

        // assert
        assertThat(actual)
            .isInstanceOf(TailoringexpertException.class)
            .hasMessage("Could not convert worksheet chapter row 3");
    }
}
