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
package eu.tailoringexpert.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.BaseCatalogVersion;
import eu.tailoringexpert.domain.BaseCatalogVersionResource;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.BaseCatalogRepository;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.IMAGE_JPEG;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Log4j2
class CatalogControllerTest {

    CatalogService serviceMock;
    BaseCatalogRepository repositoryMock;
    Function<String, MediaType> mediaTypeProviderMock;
    ObjectMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(BaseCatalogRepository.class);
        this.serviceMock = mock(CatalogService.class);
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
            IMAGE_JPEG,
            IMAGE_PNG,
            APPLICATION_OCTET_STREAM,
            APPLICATION_PDF,
            new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")
        ));

        this.mockMvc = standaloneSetup(new CatalogController(
            mapperMock,
            serviceMock,
            repositoryMock,
            mediaTypeProviderMock,
            objectMapper))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), byteArrayHttpMessageConverter)
            .build();
    }

    @Test
    void postBaseCatalog_NoError_StateNoContent() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        given(serviceMock.doImport(catalog)).willReturn(TRUE);

        // act
        ResultActions actual = mockMvc.perform(post("/catalog")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(catalog))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isCreated());

        verify(serviceMock, times(1)).doImport(catalog);
        verify(mapperMock, times(0)).toResource(any(), any(Catalog.class));
    }

    @Test
    void postBaseCatalog_Error_StatePreconditionFailed() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        given(serviceMock.doImport(catalog)).willReturn(FALSE);

        // act
        ResultActions actual = mockMvc.perform(post("/catalog")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(catalog))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isPreconditionFailed());
        assertThatNoException();
    }

    @Test
    void getBaseCatalogs_NoError_StateOK() throws Exception {
        // arrange
        PathContextBuilder pathContext = PathContext.builder();

        BaseCatalogVersion baseeCatalog7 = new BaseCatalogVersion() {
            @Override
            public String getVersion() {
                return "7.2.1";
            }

            @Override
            public ZonedDateTime getValidFrom() {
                return ZonedDateTime.now();
            }

            @Override
            public ZonedDateTime getValidUntil() {
                return null;
            }
        };
        BaseCatalogVersion baseCatalog8 = new BaseCatalogVersion() {
            @Override
            public String getVersion() {
                return "8.2.1";
            }

            @Override
            public ZonedDateTime getValidFrom() {
                return ZonedDateTime.now();
            }

            @Override
            public ZonedDateTime getValidUntil() {
                return null;
            }
        };
        ArgumentCaptor<BaseCatalogVersion> katalogCaptor = ArgumentCaptor.forClass(BaseCatalogVersion.class);
        given(repositoryMock.findCatalogVersionBy()).willReturn(asList(baseeCatalog7, baseCatalog8));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), katalogCaptor.capture()))
            .willReturn(BaseCatalogVersionResource.builder().validFrom(ZonedDateTime.now()).build());


        // act
        ResultActions actual = mockMvc.perform(get("/catalog")
            .accept(HAL_JSON_VALUE)
            .contentType(APPLICATION_JSON)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).findCatalogVersionBy();
        verify(mapperMock, times(2)).toResource(pathContextCaptor.capture(), katalogCaptor.capture());
        Assertions.assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getBaseCatalog_BaseCatalogExists_StateOk() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        given(serviceMock.getCatalog("42")).willReturn(of(catalog));

        // act
        ResultActions actual = mockMvc.perform(get("/catalog/42")
            .accept(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(catalog))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isOk());
        assertThatNoException();
    }

    @Test
    void getBaseCatalog_BaseCatalogNotExists_StateNotFound() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();
        given(serviceMock.getCatalog("42")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/catalog/42")
            .accept(APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(catalog))
            .contentType(APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getBaseCatalogPrint_BaseCatalogNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.createCatalog("8.2.1")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/catalog/8.2.1/pdf"));

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getBaseCatalogPrint_BaseCatalogExists_StateOK() throws Exception {
        // arrange
        byte[] data;
        // file content not important. only size of byte[]
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }
        given(serviceMock.createCatalog("8.2.1"))
            .willReturn(of(File.builder()
                .name("DOC-CAT-001.pdf")
                .data(data)
                .build()));

        given(mediaTypeProviderMock.apply("pdf"))
            .willReturn(APPLICATION_PDF);

        // act
        ResultActions actual = mockMvc.perform(get("/catalog/8.2.1/pdf"));

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(ATTACHMENT).filename("DOC-CAT-001.pdf").build().toString()))
            .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION))
            .andExpect(content().contentType(APPLICATION_PDF))
            .andExpect(content().bytes(data));

        verify(mediaTypeProviderMock, times(1)).apply("pdf");
    }

    @Test
    void getBaseCatalogJson_BaseCatalogNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.getCatalog("8.2.1"))
            .willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/catalog/8.2.1/json"));

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getBaseCatalogJson_BaseCatalogExists_StateOk() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/basecatalog.json"))) {
            assert nonNull(is);

            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }
        given(serviceMock.getCatalog("8.2.1"))
            .willReturn(of(catalog));

        given(mediaTypeProviderMock.apply("json"))
            .willReturn(APPLICATION_JSON);

        // act
        ResultActions actual = mockMvc.perform(get("/catalog/8.2.1/json"));

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(ATTACHMENT).filename("catalog_v8.2.1.json").build().toString()))
            .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION))
            .andExpect(content().contentType(APPLICATION_JSON));

        verify(mediaTypeProviderMock, times(1)).apply("json");
        assertThatNoException();
    }

    @Test
    void getDocuments_BaseCatalogNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.createCatalog("8.2.1")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/catalog/8.2.1/document"));

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getDocuments_BaseCatalogExists_StateOK() throws Exception {
        // arrange
        byte[] data;
        // file content not important. only size of byte[]
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(os);
        ZipEntry zipEntry = new ZipEntry("dummy.pdf");
        zip.putNextEntry(zipEntry);
        zip.write(data, 0, data.length);
        zip.closeEntry();
        zip.close();

        given(serviceMock.createDocuments("8.2.1"))
            .willReturn(of(File.builder().data(os.toByteArray()).name("catalog_8.2.1.zip").build()));

        given(mediaTypeProviderMock.apply("zip"))
            .willReturn(APPLICATION_OCTET_STREAM);

        // act
        ResultActions actual = mockMvc.perform(get("/catalog/8.2.1/document"));

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(ATTACHMENT).filename("catalog_8.2.1.zip").build().toString()))
            .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION))
            .andExpect(content().contentType(APPLICATION_OCTET_STREAM))
            .andExpect(content().bytes(os.toByteArray()));

        verify(mediaTypeProviderMock, times(1)).apply("zip");
    }
}

