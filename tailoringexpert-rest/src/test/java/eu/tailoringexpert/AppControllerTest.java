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
package eu.tailoringexpert;

import eu.tailoringexpert.domain.ResourceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule.HalHandlerInstantiator;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;

import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Locale.GERMANY;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class AppControllerTest {

    JsonMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mapperMock = mock(ResourceMapper.class);

        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        byteArrayHttpMessageConverter.setSupportedMediaTypes(asList(
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.APPLICATION_PDF,
            new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")
        ));

        this.objectMapper = JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd", GERMANY))
            .findAndAddModules()
            .addModule(new HalJacksonModule())
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .handlerInstantiator(new HalHandlerInstantiator(new EvoInflectorLinkRelationProvider(),
                CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY))
            .build();

        this.mockMvc = standaloneSetup(new AppController(mapperMock))
            .setControllerAdvice(new ExceptionHandlerAdvice())
            .setMessageConverters(
                new JacksonJsonHttpMessageConverter(objectMapper),
                byteArrayHttpMessageConverter)
            .build();
    }

    @Test
    void getLinks_NoParameterRequired_LinksReturned() throws Exception {
        // arrange
        Map<String, String> parameter = Collections.emptyMap();

        BDDMockito.given(mapperMock.createLink(anyString(), anyString(), anyMap()))
            .willAnswer(invocation -> Link.of(invocation.getArgument(0)));

        // act
        mockMvc.perform(get("/").accept(HAL_JSON_VALUE));

        // verify
        verify(mapperMock, times(1)).createLink("catalog", ResourceMapper.BASECATALOG, parameter);
        verify(mapperMock, times(1)).createLink("projects", ResourceMapper.PROJECTS, parameter);
        verify(mapperMock, times(1)).createLink("screeningsheet", ResourceMapper.SCREENINGSHEET, parameter);
        verify(mapperMock, times(1)).createLink("selectionvector", ResourceMapper.SELECTIONVECTOR_PROFILE, parameter);
        verify(mapperMock, times(1)).createLink("project", ResourceMapper.PROJECT, parameter);
    }

}
