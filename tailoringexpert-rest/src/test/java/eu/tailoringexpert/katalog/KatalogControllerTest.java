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
package eu.tailoringexpert.katalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.KatalogVersion;
import eu.tailoringexpert.domain.KatalogVersionResource;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.KatalogRepository;
import lombok.extern.log4j.Log4j2;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static eu.tailoringexpert.domain.MediaTypeProvider.ATTACHMENT;
import static eu.tailoringexpert.domain.MediaTypeProvider.FORM_DATA;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.file.Files.newInputStream;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMANY;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Log4j2
class KatalogControllerTest {

    KatalogService serviceMock;
    KatalogRepository repositoryMock;
    Function<String, MediaType> mediaTypeProviderMock;
    ObjectMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(KatalogRepository.class);
        this.serviceMock = mock(KatalogService.class);
        this.mapperMock = mock(ResourceMapper.class);
        this.mediaTypeProviderMock = mock(Function.class);

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
            APPLICATION_PDF,
            new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")
        ));

        this.mockMvc = standaloneSetup(new KatalogController(
            mapperMock,
            serviceMock,
            repositoryMock,
            mediaTypeProviderMock,
            objectMapper))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), byteArrayHttpMessageConverter)
            .build();
    }

    @Test
    void importKatalogDefinition_FehlerfreierImport_StatusNoContent() throws Exception {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder().build();
        given(serviceMock.doImport(katalog)).willReturn(TRUE);

        // act
        ResultActions actual = mockMvc.perform(post("/katalog")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(katalog))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isCreated());

        verify(serviceMock, times(1)).doImport(katalog);
        verify(mapperMock, times(0)).toResource(any(), any(Katalog.class));
    }

    @Test
    void importKatalogDefinition_FehlerhafterImport_StatusBadRequest() throws Exception {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder().build();
        given(serviceMock.doImport(katalog)).willReturn(FALSE);

        // act
        ResultActions actual = mockMvc.perform(post("/katalog")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(katalog))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isBadRequest());
        assertThatNoException();
    }

    @Test
    void getKataloge_KeinFehler_StatusOKUndLinks() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();

        KatalogVersion katalog7 = new KatalogVersion() {
            @Override
            public String getVersion() {
                return "7.2.1";
            }

            @Override
            public ZonedDateTime getGueltigAb() {
                return ZonedDateTime.now();
            }

            @Override
            public ZonedDateTime getGueltigBis() {
                return null;
            }
        };
        KatalogVersion katalog8 = new KatalogVersion() {
            @Override
            public String getVersion() {
                return "8.2.1";
            }

            @Override
            public ZonedDateTime getGueltigAb() {
                return ZonedDateTime.now();
            }

            @Override
            public ZonedDateTime getGueltigBis() {
                return null;
            }
        };
        ArgumentCaptor<KatalogVersion> katalogCaptor = ArgumentCaptor.forClass(KatalogVersion.class);
        given(repositoryMock.findKatalogVersionBy()).willReturn(asList(katalog7, katalog8));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), katalogCaptor.capture()))
            .willReturn(KatalogVersionResource.builder().gueltigAb(ZonedDateTime.now()).build());


        // act
        ResultActions actual = mockMvc.perform(get("/katalog")
            .accept(HAL_JSON_VALUE)
            .contentType(APPLICATION_JSON)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).findKatalogVersionBy();
        verify(mapperMock, times(2)).toResource(pathContextCaptor.capture(), katalogCaptor.capture());
        Assertions.assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getKatalog_KatalogVorhanden_KatalogUndStatusOK() throws Exception {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder().build();
        given(serviceMock.getKatalog("42")).willReturn(of(katalog));

        // act
        ResultActions actual = mockMvc.perform(get("/katalog/42")
            .accept(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(katalog))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isOk());
        assertThatNoException();
    }

    @Test
    void getKatalog_KatalogNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder().build();
        given(serviceMock.getKatalog("42")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/katalog/42")
            .accept(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(katalog))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void createKatalog_KatalogVorhanden_DateiUndStatusOK() throws Exception {
        // arrange
        byte[] data;
        // file content not important. only size of byte[]
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }
        given(serviceMock.createKatalog("8.2.1"))
            .willReturn(of(Datei.builder()
                .docId("DOC-CAT-001")
                .type("pdf")
                .bytes(data)
                .build()));

        given(mediaTypeProviderMock.apply("pdf"))
            .willReturn(APPLICATION_PDF);

        // act
        ResultActions actual = mockMvc.perform(get("/katalog/8.2.1/pdf"));

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(ATTACHMENT).filename("DOC-CAT-001.pdf").build().toString()))
            .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION))
            .andExpect(content().contentType(APPLICATION_PDF))
            .andExpect(content().bytes(data));

        verify(mediaTypeProviderMock, times(1)).apply("pdf");
    }

    @Test
    void getJsonKatalog_KatalogNichtVorhanden_DateiUndStatusOK() throws Exception {
        // arrange
        given(serviceMock.getKatalog("8.2.1"))
            .willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/katalog/8.2.1/json"));

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getJsonKatalog_KatalogVorhanden_DateiUndStatusOK() throws Exception {
        // arrange
        Katalog<KatalogAnforderung> katalog;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/katalog.json"))) {
            assert nonNull(is);

            katalog = objectMapper.readValue(is, new TypeReference<Katalog<KatalogAnforderung>>() {
            });
        }
        given(serviceMock.getKatalog("8.2.1"))
            .willReturn(of(katalog));

        given(mediaTypeProviderMock.apply("json"))
            .willReturn(APPLICATION_JSON);

        // act
        ResultActions actual = mockMvc.perform(get("/katalog/8.2.1/json"));

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(ATTACHMENT).filename("katalog_v8.2.1.json").build().toString()))
            .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION))
            .andExpect(content().contentType(APPLICATION_JSON));

        verify(mediaTypeProviderMock, times(1)).apply("json");
        assertThatNoException();
    }
}

