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

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.text.SimpleDateFormat;
import java.util.List;

import static java.util.Locale.GERMANY;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class TailoringCatalogChapterResourcePrimevueMixInTest {

    JsonMapper objectMapper;

    @BeforeEach
    void setup() {

        this.objectMapper = JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd", GERMANY))
            .addModule(new HalJacksonModule())
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .handlerInstantiator(new HalJacksonModule.HalHandlerInstantiator(new EvoInflectorLinkRelationProvider(),
                CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY))
            .addMixIn(TailoringCatalogChapterResource.class, TailoringCatalogChapterResourcePrimevueMixIn.class)
            .build();
    }

    @Test
    void serialize_MixInAvailable_JSONAccordingToMixInPropNames() throws JacksonException {
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
        assertThat(json.has("id")).isFalse();
        assertThat(json.has("key")).isTrue();
        assertThat(json.has("number")).isFalse();
        assertThat(json.has("label")).isTrue();
        assertThat(json.has("chapterName")).isFalse();
        assertThat(json.has("requirements")).isFalse();
        assertThat(json.has("data")).isTrue();
        assertThat(json.has("chapters")).isFalse();
        assertThat(json.has("children")).isTrue();
    }

}
