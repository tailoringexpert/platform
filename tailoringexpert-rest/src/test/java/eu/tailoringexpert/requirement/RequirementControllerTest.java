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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.TailoringCatalogChapterResource;
import eu.tailoringexpert.domain.TailoringRequirementResource;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import lombok.extern.log4j.Log4j2;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMANY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Log4j2
class RequirementControllerTest {

    RequirementService serviceMock;
    RequirementServiceRepository repositoryMock;
    JsonMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(RequirementServiceRepository.class);
        this.serviceMock = mock(RequirementService.class);
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

        this.mockMvc = standaloneSetup(new RequirementController(
            mapperMock,
            serviceMock,
            repositoryMock))
            .setMessageConverters(
                new JacksonJsonHttpMessageConverter(objectMapper),
                byteArrayHttpMessageConverter)
            .build();
    }

    @Test
    void getRequirement_RequirementExists_StateOk() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .chapter("1.1");

        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("a")
            .text("Anforderungstext")
            .selected(TRUE)
            .build();
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(Optional.of(anforderung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(anforderung))).willReturn(TailoringRequirementResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}", "SAMPLE", "master", "1.1", "a")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).getRequirement("SAMPLE", "master", "1.1", "a");
        verify(mapperMock, times(1)).toResource(any(), eq(anforderung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getRequirement_RequirementNotExists_StateNotFound() throws Exception {
        // arrange
        given(repositoryMock.getRequirement("SAMPLE", "master", "1.1", "a")).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}", "SAMPLE", "master", "1.1", "a")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        assertThatNoException();
        actual.andExpect(status().isNotFound());
    }

    @Test
    void putRequirementsState_RequirementExists_StateOk() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .chapter("1.1")
            .requirment("a");

        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("a")
            .text("Anforderungstext")
            .selected(TRUE)
            .build();

        given(serviceMock.handleSelected("SAMPLE", "master", "1.1", "a", FALSE)).willReturn(Optional.of(anforderung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(anforderung))).willReturn(TailoringRequirementResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}/selected/{selected}", "SAMPLE", "master", "1.1", "a", FALSE)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).handleSelected("SAMPLE", "master", "1.1", "a", false);
        verify(mapperMock, times(1)).toResource(any(), eq(anforderung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }


    @Test
    void putRequirementsState_RequirementNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.handleSelected("SAMPLE", "master", "1.1", "a", FALSE)).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}/selected/{selected}", "SAMPLE", "master", "1.1", "a", FALSE)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void putRequirementText_RequirementExists_StateOk() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .chapter("1.1")
            .requirment("a");

        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("a")
            .selected(TRUE)
            .text("Dies ist ein neuer Text")
            .build();
        given(serviceMock.handleText("SAMPLE", "master", "1.1", "a", "Dies ist ein neuer Text")).willReturn(Optional.of(anforderung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(anforderung))).willReturn(TailoringRequirementResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}/text", "SAMPLE", "master", "1.1", "a")
            .accept(HAL_JSON_VALUE)
            .param("text", "Dies ist ein neuer Text")
            .contentType(APPLICATION_FORM_URLENCODED)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).handleText("SAMPLE", "master", "1.1", "a", "Dies ist ein neuer Text");
        verify(mapperMock, times(1)).toResource(any(), eq(anforderung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void putRequirementText_RequirementNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.handleText("SAMPLE", "master", "1.1", "a", "Dies ist ein neuer Text")).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}/text", "SAMPLE", "master", "1.1", "a")
            .accept(HAL_JSON_VALUE)
            .param("text", "Dies ist ein neuer Text")
            .contentType(APPLICATION_FORM_URLENCODED)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void putRequirementsState_ChapterExists_StateOk() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .chapter("1.1");

        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1.1")
            .requirements(asList(
                TailoringRequirement.builder()
                    .position("a")
                    .selected(TRUE)
                    .build()
            ))
            .chapters(asList(
                Chapter.<TailoringRequirement>builder()
                    .number("1.1.1")
                    .build()
            ))
            .build();

        given(serviceMock.handleSelected("SAMPLE", "master", "1.1", TRUE)).willReturn(Optional.of(chapter));
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(chapter))).willReturn(TailoringCatalogChapterResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/selected/{selected}", "SAMPLE", "master", "1.1", TRUE)
            .accept(HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)

        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).handleSelected("SAMPLE", "master", "1.1", true);
        verify(mapperMock, times(1)).toResource(any(), eq(chapter));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void putRequirementsState_ChapterNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.handleSelected("SAMPLE", "master", "1.1", TRUE)).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/selected/{selected}", "SAMPLE", "master", "1.1", TRUE)
            .accept(HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)

        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void postRequirement_ChapterExists_StateOk() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .project("SAMPLE")
            .tailoring("master")
            .chapter("1.1");

        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("a1")
            .selected(TRUE)
            .text("Dies ist eine neue Requirement")
            .build();

        given(serviceMock.createRequirement("SAMPLE", "master", "1.1", "a1", "Dies ist eine neue Requirement"))
            .willReturn(Optional.of(anforderung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(anforderung))).willReturn(TailoringRequirementResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(post("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/{requirement}", "SAMPLE", "master", "1.1", "a1")
            .param("text", "Dies ist eine neue Requirement")
            .contentType(APPLICATION_FORM_URLENCODED_VALUE)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isCreated());

        verify(serviceMock, times(1)).createRequirement("SAMPLE", "master", "1.1", "a1", "Dies ist eine neue Requirement");
        verify(mapperMock, times(1)).toResource(any(), eq(anforderung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void postRequirement_ChapterNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.createRequirement("SAMPLE", "master", "1.1", "a1", "Dies ist eine neue Requirement")).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(post("/project/{project}/tailoring/{tailoring}/catalog/{kapitel}/{anforderung}", "SAMPLE", "master", "1.1", "a1")
            .param("text", "Dies ist eine neue Requirement")
            .contentType(APPLICATION_FORM_URLENCODED_VALUE)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

}

