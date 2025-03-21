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
package eu.tailoringexpert.tailoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import eu.tailoringexpert.ExceptionHandlerAdvice;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.FileResource;
import eu.tailoringexpert.domain.DocumentSignatureResource;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.NoteResource;
import eu.tailoringexpert.domain.SelectionVectorProfileResource;
import eu.tailoringexpert.domain.TailoringCatalogResource;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetResource;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.SelectionVectorResource;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringRequirementResource;
import eu.tailoringexpert.domain.TailoringInformation;
import eu.tailoringexpert.domain.TailoringResource;
import eu.tailoringexpert.domain.TailoringCatalogChapterResource;
import eu.tailoringexpert.domain.TailoringState;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static eu.tailoringexpert.domain.MediaTypeProvider.ATTACHMENT;
import static eu.tailoringexpert.domain.MediaTypeProvider.FORM_DATA;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static java.util.Locale.GERMANY;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Log4j2
class TailoringControllerTest {

    TailoringService serviceMock;
    TailoringServiceRepository repositoryMock;

    AttachmentService attachmentServiceMock;
    Function<String, MediaType> mediaTypeProviderMock;

    ObjectMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.serviceMock = mock(TailoringService.class);
        this.repositoryMock = mock(TailoringServiceRepository.class);
        this.attachmentServiceMock = mock(AttachmentService.class);
        this.mediaTypeProviderMock = mock(Function.class);
        this.mapperMock = mock(ResourceMapper.class);

        this.objectMapper = Jackson2ObjectMapperBuilder.json()
            .modules(new Jackson2HalModule(), new JavaTimeModule(), new ParameterNamesModule(), new Jdk8Module())
            .featuresToEnable(FAIL_ON_UNKNOWN_PROPERTIES)
            .featuresToDisable(FAIL_ON_EMPTY_BEANS)
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

        this.mockMvc = standaloneSetup(new TailoringController(
            mapperMock,
            serviceMock,
            repositoryMock,
            attachmentServiceMock,
            mediaTypeProviderMock))
            .setControllerAdvice(new ExceptionHandlerAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), byteArrayHttpMessageConverter, new StringHttpMessageConverter()).build()

        ;
    }

    @Test
    void getCatalog_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.getCatalog("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/catalog", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).getCatalog("SAMPLE", "master");
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(Catalog.class));
    }

    @Test
    void getCatalog_TailoringExists_StateOk() throws Exception {
        // arrange
        Catalog<TailoringRequirement> catalog = Catalog.<TailoringRequirement>builder()
            .toc(Chapter.<TailoringRequirement>builder().build())
            .build();
        given(serviceMock.getCatalog("SAMPLE", "master"))
            .willReturn(Optional.of(catalog));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(catalog)))
            .willReturn(TailoringCatalogResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/catalog", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getCatalog("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(catalog));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").build());
    }

    @Test
    void getChapter_ChapterNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.getChapter("SAMPLE", "master", "1.1")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/catalog/{chapter}", "SAMPLE", "master", "1.1"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).getChapter("SAMPLE", "master", "1.1");
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(Chapter.class));
    }

    @Test
    void getChapter_TailoringExists_StateOk() throws Exception {
        // arrange
        Chapter<TailoringRequirement> gruppe = Chapter.<TailoringRequirement>builder()
            .number("1.1")
            .build();
        given(serviceMock.getChapter("SAMPLE", "master", "1.1"))
            .willReturn(Optional.of(gruppe));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(gruppe)))
            .willReturn(TailoringCatalogChapterResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/catalog/{chapter}", "SAMPLE", "master", "1.1")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getChapter("SAMPLE", "master", "1.1");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(gruppe));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").build());
    }

    @Test
    void getScreeningSheet_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        ScreeningSheet screeningSheet = ScreeningSheet.builder().build();
        given(serviceMock.getScreeningSheet("SAMPLE", "master"))
            .willReturn(Optional.of(screeningSheet));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(screeningSheet)))
            .willReturn(ScreeningSheetResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/screeningsheet", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getScreeningSheet("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(screeningSheet));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").build());
    }

    @Test
    void getScreeningSheet_TailoringExists_StateOk() throws Exception {
        // arrange
        given(serviceMock.getScreeningSheet("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/screeningsheet", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());
        verify(serviceMock, times(1)).getScreeningSheet("SAMPLE", "master");
    }

    @Test
    void getScreeningSheetFile_TailoringNotExists_StatusNotFound() throws Exception {
        // arrange
        given(repositoryMock.getScreeningSheetFile("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/screeningsheet/pdf", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());
        verify(repositoryMock, times(1)).getScreeningSheetFile("SAMPLE", "master");
    }

    @Test
    void getScreeningSheetFile_TailoringExists_StateOkHeaderContentDisposition() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getScreeningSheetFile("SAMPLE", "master"))
            .willReturn(Optional.of(data));

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/screeningsheet/pdf", "SAMPLE", "master")
            .accept("application/pdf")
        );

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_TYPE, "application/pdf"));
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"screeningsheet.pdf\""))
            .andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));

        verify(repositoryMock, times(1)).getScreeningSheetFile("SAMPLE", "master");
    }

    @Test
    void getSelectionVector_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.getSelectionVector("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/selectionvector", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).getSelectionVector("SAMPLE", "master");
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(SelectionVectorProfile.class));
    }

    @Test
    void getSelectionVector_TailoringExists_StateOk() throws Exception {
        // arrange
        SelectionVector selectionVector = SelectionVector.builder().build();
        given(serviceMock.getSelectionVector("SAMPLE", "master")).willReturn(Optional.of(selectionVector));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(selectionVector)))
            .willReturn(SelectionVectorResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/selectionvector", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getSelectionVector("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(selectionVector));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").build());
    }

    @Test
    void getTailoring_TailoringExists_StateOk() throws Exception {
        // arrange
        Tailoring tailoring = Tailoring.builder().name("master").build();
        given(repositoryMock.getTailoring("SAMPLE", "master"))
            .willReturn(Optional.of(tailoring));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(tailoring)))
            .willReturn(TailoringResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}", "SAMPLE", "master")
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());
        verify(repositoryMock, times(1)).getTailoring("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(tailoring));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").build());
    }

    @Test
    void getTailoring_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(repositoryMock.getProject("SAMPLE"))
            .willReturn(Optional.of(Project.builder().tailoring(Tailoring.builder().build()).build()));

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}", "SAMPLE2", "master")
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void postFile_TailoringExists_StateOk() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile dokument = new MockMultipartFile("datei", "DUMMY_CM.pdf",
            "text/plain", data);

        Tailoring tailoring = Tailoring.builder()
            .name("master")
            .build();

        File toSave = File.builder().name("DUMMY_CM.pdf").data(data).build();
        given(attachmentServiceMock.save("SAMPLE", "master", toSave))
            .willReturn(Optional.of(toSave));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(tailoring)))
            .willReturn(TailoringResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(multipart("/project/{project}/tailoring/{tailoring}/attachment", "SAMPLE", "master")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/project/SAMPLE/tailoring/master/attachment/DUMMY_CM.pdf"));

        verify(attachmentServiceMock, times(1)).save("SAMPLE", "master", toSave);
    }

    @Test
    void postFile_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile dokument = new MockMultipartFile("datei", "DUMMY_CM.pdf",
            "text/plain", data);

        File toSave = File.builder().name("DUMMY_CM.pdf").data(data).build();
        given(attachmentServiceMock.save("SAMPLE", "master", toSave))
            .willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(multipart("/project/{project}/tailoring/{tailoring}/attachment", "SAMPLE", "master")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(attachmentServiceMock, times(1)).save("SAMPLE", "master", toSave);
        verify(mapperMock, times(0)).toResource(any(), any(Tailoring.class));

        assertThatNoException();
    }

    @Test
    void getRequirementFile_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.createRequirementDocument("SAMPLE", "master")).willReturn(empty());


        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/document/catalog", "SAMPLE", "master")
            .accept("application/pdf")
        );

        // assert
        actual.andExpect(status().isNotFound());
        verify(serviceMock, times(1)).createRequirementDocument("SAMPLE", "master");
        assertThatNoException();
    }

    @Test
    void getRequirementFile_TailoringExists_StateOkHeaderContentDisposition() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(serviceMock.createRequirementDocument("SAMPLE", "master"))
            .willReturn(Optional.of(File.builder()
                .name("DUMMY-RD-PS-DLR-1000-DV-8.2.1_01.01.2021_Product Assurance Safety Sustainability Requirements for DUMMY.pdf")
                .data(data)
                .build()
            ));

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/document/catalog", "SAMPLE", "master")
            .accept("application/pdf")
        );

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_TYPE, "application/pdf"));
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"DUMMY-RD-PS-DLR-1000-DV-8.2.1_01.01.2021_Product Assurance Safety Sustainability Requirements for DUMMY.pdf\""))
            .andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));

        assertThatNoException();
    }

    @Test
    void getRequirementFile_TailoringNotExists_StateNoFound() throws Exception {
        // arrange
        given(serviceMock.createRequirementDocument("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/document", "SAMPLE", "master")
            .accept("application/zip")
        );

        // assert

        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getRequirements_ChapterExists_StateOk() throws Exception {
        // arrange
        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("a")
            .selected(TRUE)
            .build();
        given(serviceMock.getRequirements("SAMPLE", "master", "1.1"))
            .willReturn(Optional.of(of(anforderung)));

        ArgumentCaptor<TailoringRequirement> anforderungCaptor = forClass(TailoringRequirement.class);
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), anforderungCaptor.capture())).willReturn(TailoringRequirementResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/catalog/{chapter}/requirement", "SAMPLE", "master", "1.1")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), anforderungCaptor.capture());
        assertThat(anforderungCaptor.getValue()).isEqualTo(anforderung);
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").chapter("1.1").build());
    }

    @Test
    void getSigntures_TailoringExists_StateOk() throws Exception {
        // arrange
        DocumentSignature zeichnung = DocumentSignature.builder()
            .faculty("Software")
            .build();
        given(serviceMock.getDocumentSignatures("SAMPLE", "master"))
            .willReturn(Optional.of(of(zeichnung)));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(zeichnung)))
            .willReturn(DocumentSignatureResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/signature", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getDocumentSignatures("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(zeichnung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").build());
    }

    @Test
    void updateDocumentSignature_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        DocumentSignature zeichnung = DocumentSignature.builder()
            .faculty("Software")
            .build();

        given(serviceMock.updateDocumentSignature("SAMPLE", "master", zeichnung))
            .willReturn(empty());

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(zeichnung)))
            .willReturn(DocumentSignatureResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/signature/{faculty}", "SAMPLE", "master", "Software")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(zeichnung))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void updateDocumentSignature_TailoringNotExists_StateOk() throws Exception {
        // arrange
        DocumentSignature zeichnung = DocumentSignature.builder()
            .faculty("Software")
            .build();
        given(serviceMock.updateDocumentSignature("SAMPLE", "master", zeichnung))
            .willReturn(Optional.of(zeichnung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(zeichnung)))
            .willReturn(DocumentSignatureResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/signature/{faculty}", "SAMPLE", "master", "Software")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(zeichnung))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).updateDocumentSignature("SAMPLE", "master", zeichnung);
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(zeichnung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").build());
    }

    @Test
    void putName_TailoringExistsNewNameNotUsed_StateOk() throws Exception {
        // arrange
        TailoringInformation projektPhase = TailoringInformation.builder()
            .name("test")
            .build();
        given(serviceMock.updateName("SAMPLE", "master", "test"))
            .willReturn(Optional.of(projektPhase));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(projektPhase)))
            .willReturn(TailoringResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/name", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
            .param("name", "test")
            .contentType(APPLICATION_FORM_URLENCODED_VALUE)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).updateName("SAMPLE", "master", "test");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(projektPhase));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("test").build());
    }

    @Test
    void putName_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.updateName("SAMPLE", "master", "test"))
            .willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/name", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
            .param("name", "test")
            .contentType(APPLICATION_FORM_URLENCODED_VALUE)
            .characterEncoding(UTF_8.displayName()));

        // assert
        actual.andExpect(status().isPreconditionFailed());

        verify(serviceMock, times(1)).updateName("SAMPLE", "master", "test");
        verify(mapperMock, times(0)).toResource(any(), any(Tailoring.class));
    }


    @Test
    void getAttachmentList_TailoringExist_StateOk() throws Exception {
        // arrange
        File file1 = File.builder().build();
        File file2 = File.builder().build();

        given(attachmentServiceMock.list("SAMPLE", "master"))
            .willReturn(of(file1, file2));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), any(File.class)))
            .willReturn(FileResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/attachment", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(attachmentServiceMock, times(1)).list("SAMPLE", "master");
        verify(mapperMock, times(2)).toResource(pathContextCaptor.capture(), any(File.class));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().project("SAMPLE").tailoring("master").build());
    }

    @Test
    void getAttachmentList_TailoringNotExists_StateOk() throws Exception {
        // arrange
        given(attachmentServiceMock.list("SAMPLE", "master")).willReturn(emptyList());

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), any(File.class)))
            .willReturn(FileResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/attachment", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(attachmentServiceMock, times(1)).list("SAMPLE", "master");
        verify(mapperMock, times(0)).toResource(pathContextCaptor.capture(), any(File.class));
    }

    @Test
    void getProfiles_NoProfilesExists_StateOk() throws Exception {
        // arrange
        given(repositoryMock.getSelectionVectorProfile()).willReturn(emptyList());

        // act
        ResultActions actual = mockMvc.perform(get("/selectionvector")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).getSelectionVectorProfile();
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(File.class));
    }

    @Test
    void getProfiles_ProfilesExists_StateOkK() throws Exception {
        // arrange
        given(repositoryMock.getSelectionVectorProfile()).willReturn(asList(
            SelectionVectorProfile.builder().build(),
            SelectionVectorProfile.builder().build()
        ));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), any(SelectionVectorProfile.class)))
            .willReturn(SelectionVectorProfileResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/selectionvector")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).getSelectionVectorProfile();
        verify(mapperMock, times(2)).toResource(eq(pathContextCaptor.getValue()), any(SelectionVectorProfile.class));
    }

    @Test
    void getComparisonDocument_TailoringNotExisting_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.createComparisonDocument("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/compare", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).createComparisonDocument("SAMPLE", "master");
    }

    @Test
    void getComparisionDocument_DataExist_StatusOK() throws Exception {
        // arrange
        given(serviceMock.createComparisonDocument("SAMPLE", "master")).willReturn(Optional.of(
            File.builder()
                .data("Blindtext".getBytes(UTF_8))
                .name("DOCID_42.pdf")
                .build())
        );

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/compare", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isOk());
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"DOCID_42.pdf\""));
        actual.andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));
        actual.andExpect(content().contentType("application/json"));

        verify(serviceMock, times(1)).createComparisonDocument("SAMPLE", "master");
    }

    @Test
    void getAttachment_AttachmentNotExists_StateNotFound() throws Exception {
        // arrange
        given(attachmentServiceMock.load("SAMPLE", "master", "DOCID-42.pdf")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/attachment/{name}", "SAMPLE", "master", "DOCID-42.pdf"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(attachmentServiceMock, times(1)).load("SAMPLE", "master", "DOCID-42.pdf");
    }

    @Test
    void getAttachment_AttachmentExists_StateOk() throws Exception {
        // arrange
        given(attachmentServiceMock.load("SAMPLE", "master", "DOCID-42.pdf")).willReturn(Optional.of(
                File.builder()
                    .data("Blindtext".getBytes(UTF_8))
                    .name("DOCID_42.pdf")
                    .build()
            )
        );

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/attachment/{name}", "SAMPLE", "master", "DOCID-42.pdf"));

        // assert
        actual.andExpect(status().isOk());
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"DOCID-42.pdf\""));
        actual.andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));
        actual.andExpect(content().contentType("application/json"));

        verify(attachmentServiceMock, times(1)).load("SAMPLE", "master", "DOCID-42.pdf");
    }

    @Test
    void deleteTailoring_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.deleteTailoring("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(delete("/project/{project}/tailoring/{tailoring}", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).deleteTailoring("SAMPLE", "master");
    }

    @Test
    void deleteTailoring_ServiceError_StatePreconditionFailed() throws Exception {
        // arrange
        given(serviceMock.deleteTailoring("SAMPLE", "master")).willReturn(Optional.of(Boolean.FALSE));

        // act
        ResultActions actual = mockMvc.perform(delete("/project/{project}/tailoring/{tailoring}", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isPreconditionFailed());

        verify(serviceMock, times(1)).deleteTailoring("SAMPLE", "master");
    }

    @Test
    void deleteTailoring_TailoringExists_StateOk() throws Exception {
        // arrange
        given(serviceMock.deleteTailoring("SAMPLE", "master")).willReturn(Optional.of(TRUE));

        // act
        ResultActions actual = mockMvc.perform(delete("/project/{project}/tailoring/{tailoring}", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).deleteTailoring("SAMPLE", "master");
    }

    @Test
    void postRequirements_FileEmpty_StateAccepted() throws Exception {
        // arrange
        MockMultipartFile dokument = new MockMultipartFile("datei", "DUMMY_CM.pdf",
            "text/plain", (byte[]) null);
        // act
        ResultActions actual = mockMvc.perform(multipart("/project/{project}/tailoring/{tailoring}/requirement/import", "SAMPLE", "master")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
        );

        // assert
        actual.andExpect(status().isAccepted());

        verify(serviceMock, times(1)).updateImportedRequirements("SAMPLE", "master", new byte[0]);
    }

    @Test
    void postRequirements_FileNotEmpty_StateAccepted() throws Exception {
        // arrange
        MockMultipartFile dokument = new MockMultipartFile("datei", "DUMMY_CM.pdf",
            "text/plain", "Excel Import File".getBytes(UTF_8));
        // act
        ResultActions actual = mockMvc.perform(multipart("/project/{project}/tailoring/{tailoring}/requirement/import", "SAMPLE", "master")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
        );

        // assert
        actual.andExpect(status().isAccepted());

        verify(serviceMock, times(1)).updateImportedRequirements("SAMPLE", "master", "Excel Import File".getBytes(UTF_8));
    }

    @Test
    void getDocuments_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.createDocuments("SAMPLE", "master")).willReturn(empty());
        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/document", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getDocuments_TailoringExists_StateOk() throws Exception {
        // arrange
        byte[] data;
        // file content not important. only size of byte[]
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(serviceMock.createDocuments("SAMPLE", "master"))
            .willReturn(Optional.of(File.builder()
                .name("DOC-CAT-001.pdf")
                .data(data)
                .build()));

        given(mediaTypeProviderMock.apply("pdf"))
            .willReturn(APPLICATION_PDF);

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/document", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(ATTACHMENT).filename("DOC-CAT-001.pdf").build().toString()))
            .andExpect(header().string(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION))
            .andExpect(content().contentType(APPLICATION_PDF))
            .andExpect(content().bytes(data));

        verify(mediaTypeProviderMock, times(1)).apply("pdf");
        assertThatNoException();
    }

    @Test
    void deleteAttachment_AttachmentNotExists_StateNotFound() throws Exception {
        // arrange
        given(attachmentServiceMock.delete("SAMPLE", "master", "SAMPLE-CM-4711.pdf")).willReturn(false);
        // act
        ResultActions actual = mockMvc.perform(delete(
            "/project/{project}/tailoring/{tailoring}/attachment/{name}",
            "SAMPLE", "master", "SAMPLE-CM-4711.pdf")
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void deleteAttachment_AttachmentExists_StateOk() throws Exception {
        // arrange
        given(attachmentServiceMock.delete("SAMPLE", "master", "SAMPLE-CM-4711.pdf")).willReturn(true);
        // act
        ResultActions actual = mockMvc.perform(delete(
            "/project/{project}/tailoring/{tailoring}/attachment/{name}",
            "SAMPLE", "master", "SAMPLE-CM-4711.pdf")
        );

        // assert
        actual.andExpect(status().isOk());
        assertThatNoException();
    }

    @Test
    void getNotes_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.getNotes("SAMPLE", "master")).willReturn(empty());

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/note", "SAMPLE", "master")
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());
        verify(serviceMock, times(1)).getNotes("SAMPLE", "master");
        verify(mapperMock, times(0)).toResource(pathContextCaptor.capture(), any(Note.class));
    }

    @Test
    void getNotes_TailoringExists_StateOk() throws Exception {
        // arrange
        Note note = Note.builder().number(1).build();
        given(serviceMock.getNotes("SAMPLE", "master")).willReturn(Optional.of(of(note)));
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(note)))
            .willReturn(NoteResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/note", "SAMPLE", "master")
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());
        verify(serviceMock, times(1)).getNotes("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(note));
    }

    @Test
    void getNote_NoteExists_StateOk() throws Exception {
        // arrange
        Note note = Note.builder().number(1).build();

        given(serviceMock.getNote("SAMPLE", "master", 1)).willReturn(Optional.of(note));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(note)))
            .willReturn(NoteResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/note/{note}", "SAMPLE", "master", 1)
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());
        verify(serviceMock, times(1)).getNote("SAMPLE", "master", 1);
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(note));
    }

    @Test
    void getNote_NoteNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.getNote("SAMPLE", "master", 1)).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/tailoring/{tailoring}/note/{note}", "SAMPLE", "master", 1)
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void postNote_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.addNote("SAMPLE", "master", "Hello")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(post("/project/{project}/tailoring/{tailoring}/note", "SAMPLE", "master")
            .param("note", "Hello")
            .contentType(APPLICATION_FORM_URLENCODED_VALUE)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void postNote_TailoringExists_StateCreated() throws Exception {
        // arrange
        given(serviceMock.addNote("SAMPLE", "master", "Hello")).willReturn(Optional.of(Note.builder().number(2).build()));
        given(mapperMock.createLink(any(), any(), any())).willReturn(Link.of("/project/SAMPLE/tailoring/master/note/2"));

        // act
        ResultActions actual = mockMvc.perform(post("/project/{project}/tailoring/{tailoring}/note", "SAMPLE", "master")
            .param("note", "Hello")
            .contentType(APPLICATION_FORM_URLENCODED)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isCreated());
        verify(serviceMock, times(1)).addNote("SAMPLE", "master", "Hello");
    }

    @Test
    void putState_TailoringNotExists_StateNotFound() throws Exception {
        // arrange
        given(serviceMock.updateState("SAMPLE", "master", TailoringState.AGREED)).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/state/{state}", "SAMPLE", "master", TailoringState.AGREED));

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void putState_TailoringExists_StateCreated() throws Exception {
        // arrange
        TailoringInformation tailoringInformation = TailoringInformation.builder().build();
        given(serviceMock.updateState("SAMPLE", "master", TailoringState.AGREED))
            .willReturn(Optional.of(tailoringInformation));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(tailoringInformation)))
            .willReturn(TailoringResource.builder().build());


        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/tailoring/{tailoring}/state/{state}", "SAMPLE", "master", TailoringState.AGREED));

        // assert
        actual.andExpect(status().isOk());
        verify(serviceMock, times(1)).updateState("SAMPLE", "master", TailoringState.AGREED);
    }
}
