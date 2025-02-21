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
package eu.tailoringexpert.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.text.SimpleDateFormat;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static java.util.Locale.GERMANY;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class TailoringCatalogChapterResourceVuetifyMixInTest {

    ObjectMapper objectMapper;

    @BeforeEach
    void setup(){

        this.objectMapper = Jackson2ObjectMapperBuilder.json()
            .modules(new Jackson2HalModule(), new JavaTimeModule(), new ParameterNamesModule(), new Jdk8Module())
            .featuresToEnable(FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToDisable(FAIL_ON_EMPTY_BEANS)
            .visibility(FIELD, ANY)
            .dateFormat(new SimpleDateFormat("yyyy-MM-dd", GERMANY))
            .handlerInstantiator(
                new Jackson2HalModule.HalHandlerInstantiator(new EvoInflectorLinkRelationProvider(),
                    CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY))
            .mixIn(TailoringCatalogChapterResource.class, TailoringCatalogChapterResourceVuetiifyMixIn.class)
            .build();
    }

    @Test
    void serialize_MixInAvailable_JSONAccordingToMixInPropNames() throws JsonProcessingException {
        // arrange
        TailoringCatalogChapterResource resource = TailoringCatalogChapterResource.builder()
            .chapters(List.of(
                TailoringCatalogChapterResource.builder().number("1").name("sample").build()
                ))
            .build();

        // act
        String actual = objectMapper.writeValueAsString(resource);

        // assert
        log.debug(actual);
        JsonNode json = objectMapper.readTree(actual);
        assertThat(json.has("id")).isTrue();
        assertThat(json.has("number")).isFalse();
        assertThat(json.has("label")).isTrue();
        assertThat(json.has("chapterName")).isFalse();
        assertThat(json.has("nodes")).isTrue();
        assertThat(json.has("chapters")).isFalse();
    }

}
