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
package de.baedorf.tailoringexpert.anforderung;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.baedorf.tailoringexpert.domain.PathContext;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.domain.TailoringAnforderungResource;
import de.baedorf.tailoringexpert.domain.TailoringKatalogKapitelResource;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.domain.PathContext.PathContextBuilder;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Log4j2
class AnforderungControllerTest {

    AnforderungService serviceMock;
    AnforderungServiceRepository repositoryMock;
    ObjectMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(AnforderungServiceRepository.class);
        this.serviceMock = mock(AnforderungService.class);
        this.mapperMock = mock(ResourceMapper.class);

        this.objectMapper = Jackson2ObjectMapperBuilder.json()
            .modules(new Jackson2HalModule(), new JavaTimeModule(), new ParameterNamesModule(), new Jdk8Module())
            .featuresToDisable(FAIL_ON_EMPTY_BEANS, FAIL_ON_UNKNOWN_PROPERTIES)
            .visibility(FIELD, ANY)
            .dateFormat(new SimpleDateFormat("yyyy-MM-dd", GERMANY))
            .handlerInstantiator(
                new Jackson2HalModule.HalHandlerInstantiator(new EvoInflectorLinkRelationProvider(),
                    CurieProvider.NONE.NONE, MessageResolver.DEFAULTS_ONLY))
            .build();

        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        byteArrayHttpMessageConverter.setSupportedMediaTypes(asList(
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.APPLICATION_PDF,
            new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")
        ));

        this.mockMvc = standaloneSetup(new AnforderungController(
            mapperMock,
            serviceMock,
            repositoryMock))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), byteArrayHttpMessageConverter)
            .build();
    }

    @Test
    void getAnforderung_ProjektUndPhaseUndKapitelUndAnforderungVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master")
            .kapitel("1.1");

        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("a")
            .text("Anforderungstext")
            .ausgewaehlt(TRUE)
            .build();
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(Optional.of(anforderung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(anforderung))).willReturn(TailoringAnforderungResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}", "SAMPLE", "master", "1.1", "a")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).getAnforderung("SAMPLE", "master", "1.1", "a");
        verify(mapperMock, times(1)).toResource(any(), eq(anforderung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getAnforderung_ProjektUndPhaseUndKapitelUndAnforderungNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(repositoryMock.getAnforderung("SAMPLE", "master", "1.1", "a")).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}", "SAMPLE", "master", "1.1", "a")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        assertThatNoException();
        actual.andExpect(status().isNotFound());
    }

    @Test
    void updateAnforderungStatus_ProjektUndPhaseUndKapitelUndAnforderungVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master")
            .kapitel("1.1")
            .anforderung("a");

        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("a")
            .text("Anforderungstext")
            .ausgewaehlt(TRUE)
            .build();

        given(serviceMock.handleAusgewaehlt("SAMPLE", "master", "1.1", "a", FALSE)).willReturn(Optional.of(anforderung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(anforderung))).willReturn(TailoringAnforderungResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}/ausgewaehlt/{ausgewaehlt}", "SAMPLE", "master", "1.1", "a", FALSE)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).handleAusgewaehlt("SAMPLE", "master", "1.1", "a", false);
        verify(mapperMock, times(1)).toResource(any(), eq(anforderung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }


    @Test
    void updateAnforderungStatus_ProjektUndPhaseUndKapitelUndAnforderungNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.handleAusgewaehlt("SAMPLE", "master", "1.1", "a", FALSE)).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}/ausgewaehlt/{ausgewaehlt}", "SAMPLE", "master", "1.1", "a", FALSE)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void updateAnforderungText_ProjektUndPhaseUndKapitelUndAnforderungVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master")
            .kapitel("1.1")
            .anforderung("a");

        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("a")
            .ausgewaehlt(TRUE)
            .text("Dies ist ein neuer Text")
            .build();
        given(serviceMock.handleText("SAMPLE", "master", "1.1", "a", "Dies ist ein neuer Text")).willReturn(Optional.of(anforderung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(anforderung))).willReturn(TailoringAnforderungResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}/text", "SAMPLE", "master", "1.1", "a")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString("Dies ist ein neuer Text"))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).handleText("SAMPLE", "master", "1.1", "a", "Dies ist ein neuer Text");
        verify(mapperMock, times(1)).toResource(any(), eq(anforderung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void updateAnforderungText_ProjektUndPhaseUndKapitelUndAnforderungNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.handleText("SAMPLE", "master", "1.1", "a", "Dies ist ein neuer Text")).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}/text", "SAMPLE", "master", "1.1", "a")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString("Dies ist ein neuer Text"))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void updateKapitelAnforderungStatus_ProjektUndPhaseUndKapitelVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master")
            .kapitel("1.1");

        Kapitel<TailoringAnforderung> kapitel = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1")
            .anforderungen(asList(
                TailoringAnforderung.builder()
                    .position("a")
                    .ausgewaehlt(TRUE)
                    .build()
            ))
            .kapitel(asList(
                Kapitel.<TailoringAnforderung>builder()
                    .nummer("1.1.1")
                    .build()
            ))
            .build();

        given(serviceMock.handleAusgewaehlt("SAMPLE", "master", "1.1", TRUE)).willReturn(Optional.of(kapitel));
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(kapitel))).willReturn(TailoringKatalogKapitelResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/ausgewaehlt/{ausgewaehlt}", "SAMPLE", "master", "1.1", TRUE)
            .accept(HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)

        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).handleAusgewaehlt("SAMPLE", "master", "1.1", true);
        verify(mapperMock, times(1)).toResource(any(), eq(kapitel));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void updateKapitelAnforderungStatus_ProjektUndPhaseUndKapitelNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.handleAusgewaehlt("SAMPLE", "master", "1.1", TRUE)).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/ausgewaehlt/{ausgewaehlt}", "SAMPLE", "master", "1.1", TRUE)
            .accept(HAL_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)

        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void createAnforderung_ProjektUndPhaseUndKapitelVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder()
            .projekt("SAMPLE")
            .tailoring("master")
            .kapitel("1.1");

        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("a1")
            .ausgewaehlt(TRUE)
            .text("Dies ist eine neue Anforderung")
            .build();

        given(serviceMock.createAnforderung("SAMPLE", "master", "1.1", "a1", "Dies ist eine neue Anforderung"))
            .willReturn(Optional.of(anforderung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(anforderung))).willReturn(TailoringAnforderungResource.builder().build());
        // act
        ResultActions actual = mockMvc.perform(post("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}", "SAMPLE", "master", "1.1", "a1")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString("Dies ist eine neue Anforderung"))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())

        );

        // assert
        actual.andExpect(status().isCreated());

        verify(serviceMock, times(1)).createAnforderung("SAMPLE", "master", "1.1", "a1", "Dies ist eine neue Anforderung");
        verify(mapperMock, times(1)).toResource(any(), eq(anforderung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void createAnforderung_ProjektUndPhaseUndKapitelNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.createAnforderung("SAMPLE", "master", "1.1", "a1", "Dies ist eine neue Anforderung")).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(post("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/{anforderung}", "SAMPLE", "master", "1.1", "a1")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString("Dies ist eine neue Anforderung"))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())

        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

}

