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
package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.text.SimpleDateFormat;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMANY;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ScreeningSheetControllerTest {

    ScreeningSheetService serviceMock;
    JsonMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.serviceMock = mock(ScreeningSheetService.class);
        this.mapperMock = mock(ResourceMapper.class);

        this.objectMapper = JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd", GERMANY))
            .addModule(new HalJacksonModule())
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .handlerInstantiator(new HalJacksonModule.HalHandlerInstantiator(new EvoInflectorLinkRelationProvider(),
                CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY))
            .build();


        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        byteArrayHttpMessageConverter.setSupportedMediaTypes(asList(
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.APPLICATION_PDF,
            new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")
        ));

        this.mockMvc = standaloneSetup(new ScreeningSheetController(
            mapperMock,
            serviceMock))
            .setMessageConverters(
                new JacksonJsonHttpMessageConverter(objectMapper),
                byteArrayHttpMessageConverter)
            .build();
    }

    @Test
    void postScreeningSheet_ScreeningSheetUploaded_StateOk() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();

        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile dokument = new MockMultipartFile("file", "screeningsheet_0d.pdf",
            "text/plain", data);

        ScreeningSheet screeningSheet = ScreeningSheet.builder().build();
        given(serviceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(screeningSheet))).willReturn(ScreeningSheetResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(multipart("/screeningsheet")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).createScreeningSheet(data);
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(screeningSheet));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());

    }

    @Test
    void postScreeningSheet_ScreeningSheetNotCreated_StateNotFound() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile dokument = new MockMultipartFile("file", "screeningsheet_0d.pdf",
            "text/plain", data);


        given(serviceMock.createScreeningSheet(data)).willReturn(null);

        // act
        ResultActions actual = mockMvc.perform(multipart("/screeningsheet")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }
}
