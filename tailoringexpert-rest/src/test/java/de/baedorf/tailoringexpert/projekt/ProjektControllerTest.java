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
package de.baedorf.tailoringexpert.projekt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.baedorf.tailoringexpert.domain.PathContext;
import de.baedorf.tailoringexpert.domain.PathContext.PathContextBuilder;
import de.baedorf.tailoringexpert.domain.Projekt;
import de.baedorf.tailoringexpert.domain.ProjektInformation;
import de.baedorf.tailoringexpert.domain.ProjektInformationResource;
import de.baedorf.tailoringexpert.domain.ProjektResource;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetResource;
import de.baedorf.tailoringexpert.domain.SelektionsVektor;
import de.baedorf.tailoringexpert.domain.SelektionsVektorResource;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.tailoring.TailoringService;
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
import static de.baedorf.tailoringexpert.domain.ResourceMapper.PROJEKT;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.REL_SELF;
import static java.nio.file.Files.newInputStream;
import static java.util.Arrays.asList;
import static java.util.Locale.GERMANY;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Log4j2
class ProjektControllerTest {

    ProjektService projektServiceMock;
    ProjektServiceRepository projektServiceRepositoryMock;
    TailoringService projektPhaseServiceMock;
    ObjectMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.projektServiceRepositoryMock = mock(ProjektServiceRepository.class);
        this.projektServiceMock = mock(ProjektService.class);
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

        this.mockMvc = standaloneSetup(new ProjektController(
            mapperMock,
            projektServiceMock,
            projektServiceRepositoryMock,
            projektPhaseServiceMock))
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), byteArrayHttpMessageConverter)
            .build();
    }

    @Test
    void createProject() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelektionsVektor selektionsVektor = SelektionsVektor.builder().build();
        CreateProjectTO createProject = CreateProjectTO.builder()
            .projekt("SAMPLE")
            .tailoring("master")
            .selektionsVektor(selektionsVektor)
            .build();

        given(projektServiceMock.createProjekt("8.2.1", data, selektionsVektor)).willReturn(createProject);

        ProjektAnlageRequest anlageRequest = ProjektAnlageRequest.builder()
            .screeningSheet(ScreeningSheet.builder()
                .parameters(Collections.emptyList())
                .data(data)
                .selektionsVektor(selektionsVektor)
                .build())
            .selektionsVektor(selektionsVektor)
            .build();


        given(mapperMock.createLink(REL_SELF, "http://localhost", PROJEKT, Map.of("projekt", "SAMPLE")))
            .willReturn(Link.of("http://localhost/projekt/SAMPLE", "self"));


        // act
        ResultActions actual = mockMvc.perform(post("/katalog/{version}/projekt", "8.2.1")
            .content(objectMapper.writeValueAsString(anlageRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .accept("application/hal+json")
        );

        // assert
        actual
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/projekt/SAMPLE"));

        verify(projektServiceMock, times(1)).createProjekt("8.2.1", data, selektionsVektor);
    }


    @Test
    void getProjekte() throws Exception {
        // arrange
        ProjektInformation projekt = ProjektInformation.builder().kuerzel("SAMPLE").build();
        given(projektServiceRepositoryMock.getProjektInformationen()).willReturn(asList(projekt));

        PathContextBuilder pathContext = PathContext.builder();
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(projekt))).willReturn(ProjektInformationResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(projektServiceRepositoryMock, times(1)).getProjektInformationen();
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(projekt));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());

    }

    @Test
    void getProjekt() throws Exception {
        // arrange
        ProjektInformation projekt = ProjektInformation.builder()
            .kuerzel("SAMPLE")
            .build();
        given(projektServiceRepositoryMock.getProjektInformation("SAMPLE")).willReturn(Optional.of(projekt));

        PathContextBuilder pathContext = PathContext.builder();
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(projekt))).willReturn(ProjektInformationResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(projektServiceRepositoryMock, times(1)).getProjektInformation("SAMPLE");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(projekt));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getScreeningSheet() throws Exception {
        // arrange
        ScreeningSheet screeningSheet = ScreeningSheet.builder().build();
        given(projektServiceRepositoryMock.getScreeningSheet("SAMPLE")).willReturn(Optional.of(screeningSheet));

        PathContextBuilder pathContext = PathContext.builder().projekt("SAMPLE");
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(screeningSheet))).willReturn(ScreeningSheetResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/screeningsheet", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(projektServiceRepositoryMock, times(1)).getScreeningSheet("SAMPLE");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(screeningSheet));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getScreeningSheetDatei() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(projektServiceRepositoryMock.getScreeningSheetDatei("SAMPLE")).willReturn(Optional.of(data));

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/screeningsheet/pdf", "SAMPLE")
            .accept("application/pdf")
        );

        // assert
        actual.andExpect(status().isOk());
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"screeningsheet.pdf\""))
            .andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));

        verify(projektServiceRepositoryMock, times(1)).getScreeningSheetDatei("SAMPLE");

    }

    @Test
    void deleteProjekt_ProjektVorhanden_ProjektWurdeGeloescht() throws Exception {
        // arrange
        given(projektServiceMock.deleteProjekt("SAMPLE")).willReturn(true);

        // act
        ResultActions actual = mockMvc.perform(delete("/projekt/{projekt}", "SAMPLE"));

        // assert
        actual.andExpect(status().isNoContent());

        verify(projektServiceMock, times(1)).deleteProjekt("SAMPLE");
    }

    @Test
    void deleteProjekt_ProjektNichtVorhanden_ProjektWurdeNichtGeloescht() throws Exception {
        // arrange
        given(projektServiceMock.deleteProjekt("SAMPLE")).willReturn(false);

        // act
        ResultActions actual = mockMvc.perform(delete("/projekt/{projekt}", "SAMPLE"));

        // assert
        actual.andExpect(status().isBadRequest());

        verify(projektServiceMock, times(1)).deleteProjekt("SAMPLE");
    }

    @Test
    void copyProjekt_ProjektNichtVorhanden_ProjektWurdeNichtKopiert() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile screeningSheet = new MockMultipartFile("datei", "screeningsheet_0d.pdf",
            "text/plain", data);

        given(projektServiceMock.copyProjekt("SAMPLE", data)).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(multipart("/projekt/{projekt}", "SAMPLE")
            .file(screeningSheet)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(projektServiceMock, times(1)).copyProjekt("SAMPLE", data);
        verify(mapperMock, times(0)).toResource(any(), any(Projekt.class));
    }

    @Test
    void copyProjekt_ProjektVorhande_ProjektWurdeKopiert() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile screeningSheet = new MockMultipartFile("datei", "screeningsheet_0d.pdf",
            "text/plain", data);

        Projekt createdProjekt = Projekt.builder().kuerzel("SAMPLE2").build();
        given(projektServiceMock.copyProjekt("SAMPLE", data)).willReturn(Optional.of(createdProjekt));

        given(mapperMock.createLink(REL_SELF, "http://localhost", PROJEKT, Map.of("projekt", "SAMPLE2")))
            .willReturn(Link.of("http://localhost/projekt/SAMPLE2", "self"));

        PathContextBuilder pathContext = PathContext.builder();
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(createdProjekt))).willReturn(ProjektResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(multipart("/projekt/{projekt}", "SAMPLE")
            .file(screeningSheet)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual.andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/projekt/SAMPLE2"));

        verify(projektServiceMock, times(1)).copyProjekt("SAMPLE", data);
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(createdProjekt));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void addNewProjektPhase_ProjektVorhanden_PhaseWurdeHinzugefuegt() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelektionsVektor selektionsVektor = SelektionsVektor.builder().build();
        Tailoring tailoring = Tailoring.builder()
            .name("master1")
            .build();
        given(projektServiceMock.addTailoring("SAMPLE", "8.2.1", data, selektionsVektor)).willReturn(Optional.of(tailoring));

        ProjektAnlageRequest anlageRequest = ProjektAnlageRequest.builder()
            .katalog("8.2.1")
            .screeningSheet(ScreeningSheet.builder()
                .parameters(Collections.emptyList())
                .data(data)
                .selektionsVektor(selektionsVektor)
                .build())
            .selektionsVektor(selektionsVektor)
            .build();

        // act
        ResultActions actual = mockMvc.perform(post("/projekt/{projekt}/tailoring", "SAMPLE")
            .content(objectMapper.writeValueAsString(anlageRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .accept("application/hal+json")
        );

        // assert
        actual
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/projekt/SAMPLE/tailoring/master1"));

        verify(projektServiceMock, times(1)).addTailoring("SAMPLE", "8.2.1", data, selektionsVektor);
        verify(mapperMock, times(0)).toResource(any(), any(Tailoring.class));
    }

    @Test
    void addNewProjektPhase_ProjektNichtVorhanden_PhaseWurdeNichtHinzugefuegt() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        SelektionsVektor selektionsVektor = SelektionsVektor.builder().build();
        given(projektServiceMock.addTailoring("SAMPLE", "8.2.1", data, selektionsVektor)).willReturn(Optional.empty());

        ProjektAnlageRequest anlageRequest = ProjektAnlageRequest.builder()
            .katalog("8.2.1")
            .screeningSheet(ScreeningSheet.builder()
                .parameters(Collections.emptyList())
                .data(data)
                .selektionsVektor(selektionsVektor)
                .build())
            .selektionsVektor(selektionsVektor)
            .build();

        // act
        ResultActions actual = mockMvc.perform(post("/projekt/{projekt}/tailoring", "SAMPLE")
            .content(objectMapper.writeValueAsString(anlageRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .characterEncoding(StandardCharsets.UTF_8.displayName())
            .accept("application/hal+json")
        );

        // assert
        actual
            .andExpect(status().isNotFound());

        verify(projektServiceMock, times(1)).addTailoring("SAMPLE", "8.2.1", data, selektionsVektor);
        verify(mapperMock, times(0)).toResource(any(), any(Tailoring.class));
    }

    @Test
    void getSelektionsVektor_SelektionsVektorVorhanden_SelektionsVektorWirdZureuckGegeben() throws Exception {
        // arrange
        Projekt projekt = Projekt.builder()
            .kuerzel("SAMPLE")
            .screeningSheet(ScreeningSheet.builder()
                .selektionsVektor(SelektionsVektor.builder().build())
                .build())
            .build();
        given(projektServiceRepositoryMock.getProjekt("SAMPLE")).willReturn(Optional.of(projekt));

        PathContextBuilder pathContext = PathContext.builder().projekt("SAMPLE");
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = ArgumentCaptor.forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(projekt.getScreeningSheet().getSelektionsVektor())))
            .willReturn(SelektionsVektorResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/selektionsvektor", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(projektServiceRepositoryMock, times(1)).getProjekt("SAMPLE");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(projekt.getScreeningSheet().getSelektionsVektor()));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(pathContext.build());
    }

    @Test
    void getSelektionsVektor_SelektionsVektorNichtVorhanden_SelektionsVektorWirdNichtZureuckGegeben() throws Exception {
        // arrange
        given(projektServiceRepositoryMock.getProjekt("SAMPLE")).willReturn(Optional.empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/selektionsvektor", "SAMPLE")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(projektServiceRepositoryMock, times(1)).getProjekt("SAMPLE");
        verify(mapperMock, times(0)).toResource(any(), any(SelektionsVektor.class));
    }
}

