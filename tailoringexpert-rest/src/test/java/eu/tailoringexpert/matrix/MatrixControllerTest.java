/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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
package eu.tailoringexpert.matrix;

import static eu.tailoringexpert.domain.ResourceMapper.MATRIX;
import static eu.tailoringexpert.domain.ResourceMapper.MATRIX_FILE;
import static eu.tailoringexpert.domain.ResourceMapper.REL_SELF;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.List.of;
import static java.util.Locale.GERMANY;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import eu.tailoringexpert.ExceptionHandlerAdvice;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.MatrixFile;
import eu.tailoringexpert.domain.MatrixFileMeta;
import eu.tailoringexpert.domain.MatrixFileResource;
import eu.tailoringexpert.domain.ResourceMapper;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
class MatrixControllerTest {

    Function<String, MediaType> mediaTypeProviderMock;
    MatrixService matrixServiceMock;

    JsonMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void beforeEach() {
        this.mapperMock = mock(ResourceMapper.class);
        this.mediaTypeProviderMock = mock(Function.class);
        this.matrixServiceMock = mock(MatrixService.class);

        this.objectMapper = JsonMapper.builder()
                .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd", GERMANY))
                .addModule(new HalJacksonModule())
                .enable(FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(FAIL_ON_EMPTY_BEANS)
                .handlerInstantiator(new HalJacksonModule.HalHandlerInstantiator(new EvoInflectorLinkRelationProvider(),
                        CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY))
                .build();

        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
        byteArrayHttpMessageConverter.setSupportedMediaTypes(of(
                new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")));

        this.mockMvc = standaloneSetup(new MatrixController(
                mapperMock,
                mediaTypeProviderMock,
                matrixServiceMock))
                .setControllerAdvice(new ExceptionHandlerAdvice())
                .setMessageConverters(
                        new JacksonJsonHttpMessageConverter(objectMapper),
                        byteArrayHttpMessageConverter)
                .build()

        ;
    }

    @Test
    void postMatrixFile_ValidCreateRequest_StateCreatedWithLocationHeader() throws Exception {
        // arrange
        MatrixFile matrixFile = MatrixFile.builder()
                .name("MATRIX01.xlsx")
                .data("MockedExcelFileContent".getBytes())
                .build();

        given(mapperMock.createLink(REL_SELF, MATRIX_FILE, Map.of("name", "MATRIX01.xlsx")))
                .willReturn(Link.of("http://localhost/matrixfile/MATRIX01.xlsx", "self"));

        // act
        ResultActions actual = mockMvc.perform(post("/matrixfile")
                .content(objectMapper.writeValueAsString(matrixFile))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(UTF_8.displayName())
                .accept("application/hal+json"));

        // assert
        actual
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/matrixfile/MATRIX01.xlsx"));

        verify(matrixServiceMock, times(1)).save(matrixFile);
    }

    @Test
    void getMatrixFiles_noMatrixfiles_StateOKAndLinkToSelf() throws Exception {
        // arrange

        given(matrixServiceMock.list())
                .willReturn(of());

        given(mapperMock.createLink(REL_SELF, MATRIX, emptyMap()))
                .willReturn(Link.of("http://localhost/matrixfiles"));

        // act
        ResultActions actual = mockMvc.perform(get("/matrixfile")
                .accept(HAL_JSON_VALUE));

        // assert
        actual.andExpect(status().isOk());

        verify(matrixServiceMock, times(1)).list();
        verify(mapperMock, times(0)).toResource(any(), any(MatrixFileMeta.class));
        verify(mapperMock, times(1)).createLink(REL_SELF, MATRIX, emptyMap());

    }

    @Test
    void getMatrixFiles_existingFiles_StateOKAndList() throws Exception {
        // arrange
        MatrixFileMeta file1 = MatrixFileMeta.builder().build();
        MatrixFileMeta file2 = MatrixFileMeta.builder().build();

        given(matrixServiceMock.list())
                .willReturn(of(file1, file2));

        given(mapperMock.toResource(any(), any(MatrixFileMeta.class)))
                .willReturn(MatrixFileResource.builder().build());
        given(mapperMock.createLink(REL_SELF, MATRIX, emptyMap()))
                .willReturn(Link.of("http://localhost/matrixfiles"));

        // act
        ResultActions actual = mockMvc.perform(get("/matrixfile")
                .accept(HAL_JSON_VALUE));

        // assert
        actual.andExpect(status().isOk());

        verify(matrixServiceMock, times(1)).list();
        verify(mapperMock, times(2)).toResource(any(), any(MatrixFileMeta.class));
        verify(mapperMock, times(1)).createLink(REL_SELF, MATRIX, emptyMap());
    }

    @Test
    void getMatrixfile_MatrixfileNotExists_StateNotFound() throws Exception {
        // arrange
        given(matrixServiceMock.get("MATRIX01.xlsx")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/matrixfile/{name}", "MATRIX01.xlsx"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(matrixServiceMock, times(1)).get("MATRIX01.xlsx");
    }

    @Test
    void getMatrixfile_MatrixfileExists_StateOk() throws Exception {
        // arrange
        given(matrixServiceMock.get("MATRIX01.xlsx")).willReturn(Optional.of(
                File.builder()
                        .data("Blindtext".getBytes(UTF_8))
                        .name("MATRIX01.xlsx")
                        .build()));

        // act
        ResultActions actual = mockMvc.perform(get("/matrixfile/{name}", "MATRIX01.xlsx"));

        // assert
        actual.andExpect(status().isOk());
        actual.andExpect(
                header().string("Content-Disposition", "form-data; name=\"matrix\"; filename=\"MATRIX01.xlsx\""));
        actual.andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));
        actual.andExpect(content().contentType("application/json"));

        verify(matrixServiceMock, times(1)).get("MATRIX01.xlsx");
    }

    @Test
    void deleteMatrixFile_MatrixFileNotExists_StateNotFound() throws Exception {
        // arrange
        given(matrixServiceMock.delete("MATRIX01.xlsx")).willReturn(false);

        // act
        ResultActions actual = mockMvc.perform(delete(
                "/matrixfile/{name}",
                "MATRIX01.xlsx"));

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void deleteMatrixFile_AttachmentExists_StateOk() throws Exception {
        // arrange
        given(matrixServiceMock.delete("MATRIX01.xlsx")).willReturn(true);

        // act
        ResultActions actual = mockMvc.perform(delete(
                "/matrixfile/{name}",
                "MATRIX01.xlsx"));

        // assert
        actual.andExpect(status().isOk());
        assertThatNoException();
    }

}
