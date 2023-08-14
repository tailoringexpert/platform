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
package eu.tailoringexpert.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectInformation;
import eu.tailoringexpert.domain.ProjectResource;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetResource;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.SelectionVectorResource;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.tailoring.TailoringService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static eu.tailoringexpert.domain.ProjectState.COMPLETED;
import static eu.tailoringexpert.domain.ResourceMapper.PROJECT;
import static eu.tailoringexpert.domain.ResourceMapper.REL_SELF;
import static java.nio.file.Files.newInputStream;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMANY;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Log4j2
class ProjectControllerTest {

    ProjectService projectServiceMock;
    ProjectServiceRepository projectServiceRepositoryMock;
    TailoringService projektPhaseServiceMock;
    ObjectMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.projectServiceRepositoryMock = mock(ProjectServiceRepository.class);
        this.projectServiceMock = mock(ProjectService.class);
        this.projektPhaseServiceMock = mock(TailoringService.class);
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

        this.mockMvc = standaloneSetup(new ProjectController(
            mapperMock,
            projectServiceMock,
            projectServiceRepositoryMock))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), byteArrayHttpMessageConverter)
            .build();
    }

    @Test
    void postProject_ValidCreateRequest_StateCreatedWithLocationHeader() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelectionVector selectionVector = SelectionVector.builder().build();
        CreateProjectTO createProject = CreateProjectTO.builder()
            .project("SAMPLE")
            .tailoring("master")
            .selectionVector(selectionVector)
            .build();

        given(projectServiceMock.createProject("8.2.1", data, selectionVector, null)).willReturn(createProject);

        ProjectCreationRequest anlageRequest = ProjectCreationRequest.builder()
            .screeningSheet(ScreeningSheet.builder()
                .parameters(Collections.emptyList())
                .data(data)
                .selectionVector(selectionVector)
                .build())
            .selectionVector(selectionVector)
            .build();


        given(mapperMock.createLink(REL_SELF, PROJECT, Map.of("project", "SAMPLE")))
            .willReturn(Link.of("http://localhost/project/SAMPLE", "self"));


        // act
        ResultActions actual = mockMvc.perform(post("/catalog/{version}/project", "8.2.1")
            .content(objectMapper.writeValueAsString(anlageRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .accept("application/hal+json")
        );

        // assert
        actual
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/project/SAMPLE"));

        verify(projectServiceMock, times(1)).createProject("8.2.1", data, selectionVector, null);
    }


    @Test
    void getProjects_ProjectsExist_StateOK() throws Exception {
        // arrange
        ProjectInformation projekt = ProjectInformation.builder().identifier("SAMPLE").build();
        given(projectServiceRepositoryMock.getProjectInformations()).willReturn(asList(projekt));

        PathContextBuilder pathContext = PathContext.builder();
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(projekt))).willReturn(ProjectResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(projectServiceRepositoryMock, times(1)).getProjectInformations();
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(projekt));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());

    }

    @Test
    void getProject_ProjectExist_StateOK() throws Exception {
        // arrange
        ProjectInformation projekt = ProjectInformation.builder()
            .identifier("SAMPLE")
            .build();
        given(projectServiceRepositoryMock.getProjectInformation("SAMPLE")).willReturn(Optional.of(projekt));

        PathContextBuilder pathContext = PathContext.builder();
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(projekt))).willReturn(ProjectResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(projectServiceRepositoryMock, times(1)).getProjectInformation("SAMPLE");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(projekt));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getProject_ProjectNotExist_StateNotFound() throws Exception {
        // arrange

        given(projectServiceRepositoryMock.getProjectInformation("SAMPLE")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(projectServiceRepositoryMock, times(1)).getProjectInformation("SAMPLE");
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(ProjectInformation.class));
    }

    @Test
    void getScreeningSheet_ScreeningSheetExists_StateOK() throws Exception {
        // arrange
        ScreeningSheet screeningSheet = ScreeningSheet.builder().build();
        given(projectServiceRepositoryMock.getScreeningSheet("SAMPLE")).willReturn(Optional.of(screeningSheet));

        PathContextBuilder pathContext = PathContext.builder().project("SAMPLE");
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(screeningSheet))).willReturn(ScreeningSheetResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/screeningsheet", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(projectServiceRepositoryMock, times(1)).getScreeningSheet("SAMPLE");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(screeningSheet));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getScreeningSheet_ScreeningSheetNotExists_StateNotFound() throws Exception {
        // arrange
        given(projectServiceRepositoryMock.getScreeningSheet("SAMPLE")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/screeningsheet", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(projectServiceRepositoryMock, times(1)).getScreeningSheet("SAMPLE");
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(ScreeningSheet.class));
    }

    @Test
    void getScreeningSheetFile_ScreeningSheetFileExists_StateOKContentDispositionHeader() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(projectServiceRepositoryMock.getScreeningSheetFile("SAMPLE")).willReturn(Optional.of(data));

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/screeningsheet/pdf", "SAMPLE")
            .accept("application/pdf")
        );

        // assert
        actual.andExpect(status().isOk());
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"screeningsheet.pdf\""))
            .andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));

        verify(projectServiceRepositoryMock, times(1)).getScreeningSheetFile("SAMPLE");

    }

    @Test
    void getScreeningSheetFile_ScreningsSheetFileNotExists_StateNotFound() throws Exception {
        // arrange
        given(projectServiceRepositoryMock.getScreeningSheetFile("SAMPLE")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/screeningsheet/pdf", "SAMPLE")
            .accept("application/pdf")
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(projectServiceRepositoryMock, times(1)).getScreeningSheetFile("SAMPLE");

    }


    @Test
    void deleteProject_ProjectExists_StateNoContent() throws Exception {
        // arrange
        given(projectServiceMock.deleteProject("SAMPLE")).willReturn(true);

        // act
        ResultActions actual = mockMvc.perform(delete("/project/{project}", "SAMPLE"));

        // assert
        actual.andExpect(status().isNoContent());

        verify(projectServiceMock, times(1)).deleteProject("SAMPLE");
    }

    @Test
    void deleteProjekt_ProjectNotExist_StateBadRequest() throws Exception {
        // arrange
        given(projectServiceMock.deleteProject("SAMPLE")).willReturn(false);

        // act
        ResultActions actual = mockMvc.perform(delete("/project/{project}", "SAMPLE"));

        // assert
        actual.andExpect(status().isBadRequest());

        verify(projectServiceMock, times(1)).deleteProject("SAMPLE");
    }

    @Test
    void copyProjekt_ProjectNotExists_StateNotFound() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile screeningSheet = new MockMultipartFile("datei", "screeningsheet_0d.pdf",
            "text/plain", data);

        given(projectServiceMock.copyProject("SAMPLE", data)).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(multipart("/project/{project}", "SAMPLE")
            .file(screeningSheet)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(projectServiceMock, times(1)).copyProject("SAMPLE", data);
        verify(mapperMock, times(0)).toResource(any(), any(Project.class));
    }

    @Test
    void copyProject_ProjectExists_StateCreatedLocationHeader() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile screeningSheet = new MockMultipartFile("datei", "screeningsheet_0d.pdf",
            "text/plain", data);

        Project createdProject = Project.builder().identifier("SAMPLE2").build();
        given(projectServiceMock.copyProject("SAMPLE", data)).willReturn(Optional.of(createdProject));

        given(mapperMock.createLink(REL_SELF, PROJECT, Map.of("project", "SAMPLE2")))
            .willReturn(Link.of("http://localhost/project/SAMPLE2", "self"));

        PathContextBuilder pathContext = PathContext.builder();
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(createdProject))).willReturn(ProjectResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(multipart("/project/{project}", "SAMPLE")
            .file(screeningSheet)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual.andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/project/SAMPLE2"));

        verify(projectServiceMock, times(1)).copyProject("SAMPLE", data);
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(createdProject));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void copyProject_ErrorReadingScreeningSheet_IOExceptionThrown() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile screeningSheet = new MockMultipartFile("datei", "screeningsheet_0d.pdf",
            "text/plain", data);

        MockMultipartFile spy = spy(screeningSheet);
        given(spy.getBytes()).willThrow(IOException.class);

        // act
//        Throwable actual = null;
//        try {
        Throwable actual = catchThrowable(() -> mockMvc.perform(multipart("/project/{project}", "SAMPLE")
            .file(spy)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        ));
//        } catch (Exception e) {
//        }

        // assert
        assertThat(actual).isNotNull();
        verify(projectServiceMock, times(0)).copyProject(anyString(), any(byte[].class));
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(Project.class));
    }

    @Test
    void postTailoring_ProjectExists_StateCreatedLocationHeader() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelectionVector selectionVector = SelectionVector.builder().build();
        Tailoring tailoring = Tailoring.builder()
            .name("master1")
            .build();
        given(projectServiceMock.addTailoring("SAMPLE", "8.2.1", data, selectionVector, null)).willReturn(Optional.of(tailoring));

        ProjectCreationRequest creationRequest = ProjectCreationRequest.builder()
            .catalog("8.2.1")
            .screeningSheet(ScreeningSheet.builder()
                .parameters(Collections.emptyList())
                .data(data)
                .selectionVector(selectionVector)
                .build())
            .selectionVector(selectionVector)
            .build();

        given(mapperMock.createLink(any(), any(), any())).willReturn(Link.of("/project/SAMPLE/tailoring/master1"));

        // act
        ResultActions actual = mockMvc.perform(post("/project/{project}/tailoring", "SAMPLE")
            .content(objectMapper.writeValueAsString(creationRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .accept("application/hal+json")
        );

        // assert
        actual
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/project/SAMPLE/tailoring/master1"));

        verify(projectServiceMock, times(1)).addTailoring("SAMPLE", "8.2.1", data, selectionVector, null);
        verify(mapperMock, times(0)).toResource(any(), any(Tailoring.class));
    }

    @Test
    void postTailoring_ProjectNotExists_StateNotFound() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelectionVector selectionVector = SelectionVector.builder().build();
        given(projectServiceMock.addTailoring("SAMPLE", "8.2.1", data, selectionVector, "Test")).willReturn(empty());

        ProjectCreationRequest creationRequest = ProjectCreationRequest.builder()
            .catalog("8.2.1")
            .screeningSheet(ScreeningSheet.builder()
                .parameters(Collections.emptyList())
                .data(data)
                .selectionVector(selectionVector)
                .build())
            .selectionVector(selectionVector)
            .note("Test")
            .build();

        // act
        ResultActions actual = mockMvc.perform(post("/project/{project}/tailoring", "SAMPLE")
            .content(objectMapper.writeValueAsString(creationRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .accept("application/hal+json")
        );

        // assert
        actual
            .andExpect(status().isNotFound());

        verify(projectServiceMock, times(1)).addTailoring("SAMPLE", "8.2.1", data, selectionVector, "Test");
        verify(mapperMock, times(0)).toResource(any(), any(Tailoring.class));
    }

    @Test
    void getSelectionVector_SelectionVectorExists_StateOK() throws Exception {
        // arrange
        Project project = Project.builder()
            .identifier("SAMPLE")
            .screeningSheet(ScreeningSheet.builder()
                .selectionVector(SelectionVector.builder().build())
                .build())
            .build();
        given(projectServiceRepositoryMock.getProject("SAMPLE")).willReturn(Optional.of(project));

        PathContextBuilder pathContext = PathContext.builder().project("SAMPLE");
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(project.getScreeningSheet().getSelectionVector())))
            .willReturn(SelectionVectorResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/selectionvector", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(projectServiceRepositoryMock, times(1)).getProject("SAMPLE");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(project.getScreeningSheet().getSelectionVector()));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getSelectionVector_SelectionVectorNotExists_StateNotFound() throws Exception {
        // arrange
        given(projectServiceRepositoryMock.getProject("SAMPLE")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/project/{project}/selectionvector", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(projectServiceRepositoryMock, times(1)).getProject("SAMPLE");
        verify(mapperMock, times(0)).toResource(any(), any(SelectionVector.class));
    }


    @Test
    void putState_ProjectNotExists_StateNotFound() throws Exception {
        // arrange
        given(projectServiceMock.updateState("SAMPLE", COMPLETED)).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/state/{state}", "SAMPLE", COMPLETED));

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void putState_TailoringExists_StateCreated() throws Exception {
        // arrange
        ProjectInformation projectInformation = ProjectInformation.builder().build();
        given(projectServiceMock.updateState("SAMPLE", COMPLETED))
            .willReturn(Optional.of(projectInformation));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(projectInformation)))
            .willReturn(ProjectResource.builder().build());


        // act
        ResultActions actual = mockMvc.perform(put("/project/{project}/state/{state}", "SAMPLE", COMPLETED));

        // assert
        actual.andExpect(status().isOk());
        verify(projectServiceMock, times(1)).updateState("SAMPLE", COMPLETED);
    }
}

