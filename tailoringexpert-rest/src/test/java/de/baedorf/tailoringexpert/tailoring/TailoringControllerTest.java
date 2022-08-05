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
package de.baedorf.tailoringexpert.tailoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.baedorf.tailoringexpert.ExceptionHandlerAdvice;
import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Dokument;
import de.baedorf.tailoringexpert.domain.DokumentResource;
import de.baedorf.tailoringexpert.domain.DokumentZeichnung;
import de.baedorf.tailoringexpert.domain.DokumentZeichnungResource;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogResource;
import de.baedorf.tailoringexpert.domain.PathContext;
import de.baedorf.tailoringexpert.domain.PathContext.PathContextBuilder;
import de.baedorf.tailoringexpert.domain.Projekt;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetResource;
import de.baedorf.tailoringexpert.domain.SelektionsVektor;
import de.baedorf.tailoringexpert.domain.SelektionsVektorProfil;
import de.baedorf.tailoringexpert.domain.SelektionsVektorProfilResource;
import de.baedorf.tailoringexpert.domain.SelektionsVektorResource;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.domain.TailoringAnforderungResource;
import de.baedorf.tailoringexpert.domain.TailoringInformation;
import de.baedorf.tailoringexpert.domain.TailoringInformationResource;
import de.baedorf.tailoringexpert.domain.TailoringKatalogKapitelResource;
import de.baedorf.tailoringexpert.domain.TailoringResource;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.function.Function;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
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
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@Log4j2
class TailoringControllerTest {

    TailoringService serviceMock;
    TailoringServiceRepository repositoryMock;
    Function<String, MediaType> mediaTypeProviderMock;

    ObjectMapper objectMapper;
    ResourceMapper mapperMock;
    MockMvc mockMvc;

    @BeforeEach
    void setup() throws IOException {
        this.serviceMock = mock(TailoringService.class);
        this.repositoryMock = mock(TailoringServiceRepository.class);
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
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.APPLICATION_OCTET_STREAM,
            MediaType.APPLICATION_PDF,
            new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")
        ));

        this.mockMvc = standaloneSetup(new TailoringController(
            mapperMock,
            serviceMock,
            repositoryMock,
            mediaTypeProviderMock))
            .setControllerAdvice(new ExceptionHandlerAdvice())
            .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper), byteArrayHttpMessageConverter).build()

        ;
    }

    @Test
    void getKatalog_ProjektUndPhaseNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.getKatalog("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/katalog", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).getKatalog("SAMPLE", "master");
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(Katalog.class));
    }

    @Test
    void getKatalog_ProjektUndPhaseVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        Katalog<TailoringAnforderung> katalog = Katalog.<TailoringAnforderung>builder()
            .toc(Kapitel.<TailoringAnforderung>builder().build())
            .build();
        given(serviceMock.getKatalog("SAMPLE", "master"))
            .willReturn(Optional.of(katalog));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(katalog)))
            .willReturn(KatalogResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/katalog", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getKatalog("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(katalog));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void getKapitel_ProjektUndPhaseUndKapitelNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.getKapitel("SAMPLE", "master", "1.1")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/", "SAMPLE", "master", "1.1"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).getKapitel("SAMPLE", "master", "1.1");
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(Kapitel.class));
    }

    @Test
    void getKapitel_ProjektUndPhaseUndKapitelVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        Kapitel<TailoringAnforderung> gruppe = Kapitel.<TailoringAnforderung>builder()
            .nummer("1.1")
            .build();
        given(serviceMock.getKapitel("SAMPLE", "master", "1.1"))
            .willReturn(Optional.of(gruppe));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(gruppe)))
            .willReturn(TailoringKatalogKapitelResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/", "SAMPLE", "master", "1.1")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getKapitel("SAMPLE", "master", "1.1");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(gruppe));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void getScreeningSheet_ProjektUndPhaseVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        ScreeningSheet screeningSheet = ScreeningSheet.builder().build();
        given(serviceMock.getScreeningSheet("SAMPLE", "master"))
            .willReturn(Optional.of(screeningSheet));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(screeningSheet)))
            .willReturn(ScreeningSheetResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/screeningsheet", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getScreeningSheet("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(screeningSheet));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void getScreeningSheetDatei_ProjektUndPhaseNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(repositoryMock.getScreeningSheetDatei("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/screeningsheet/pdf", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());
        verify(repositoryMock, times(1)).getScreeningSheetDatei("SAMPLE", "master");
    }

    @Test
    void getScreeningSheetDatei_ProjektUndPhaseVorhanden_DateiWirdZurueckGegeben() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getScreeningSheetDatei("SAMPLE", "master"))
            .willReturn(Optional.of(data));

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/screeningsheet/pdf", "SAMPLE", "master")
            .accept("application/pdf")
        );

        // assert
        actual.andExpect(status().isOk())
            .andExpect(header().string(CONTENT_TYPE, "application/pdf"));
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"screeningsheet.pdf\""))
            .andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));

        verify(repositoryMock, times(1)).getScreeningSheetDatei("SAMPLE", "master");
    }

    @Test
    void getSelektionsVektor_ProjektUndPhaseNichtVorhanden_StatusNotDound() throws Exception {
        // arrange
        given(serviceMock.getSelektionsVektor("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/selektionsvektor", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).getSelektionsVektor("SAMPLE", "master");
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(SelektionsVektorProfil.class));
    }

    @Test
    void getSelektionsVektor_ProjektUndPhaseVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        SelektionsVektor selektionsVektor = SelektionsVektor.builder().build();
        given(serviceMock.getSelektionsVektor("SAMPLE", "master")).willReturn(Optional.of(selektionsVektor));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(selektionsVektor)))
            .willReturn(SelektionsVektorResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/selektionsvektor", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getSelektionsVektor("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(selektionsVektor));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void getProjektPhase_ProjektUndPhaseVorhanden_StatusOkUndLinks() throws Exception {
        // arrange
        Tailoring tailoring = Tailoring.builder().name("master").build();
        given(repositoryMock.getTailoring("SAMPLE", "master"))
            .willReturn(Optional.of(tailoring));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(tailoring)))
            .willReturn(TailoringResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}", "SAMPLE", "master")
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());
        verify(repositoryMock, times(1)).getTailoring("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(tailoring));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void getProjektPhase_ProjektNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(repositoryMock.getProjekt("SAMPLE"))
            .willReturn(Optional.of(Projekt.builder().tailoring(Tailoring.builder().build()).build()));

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/phase/{phase}", "H3SAT", "master")
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getProjektPhase_ProjektPhaseNichtVorhanden_StatusPreconditionFailed() throws Exception {
        // arrange
        given(repositoryMock.getProjekt("SAMPLE"))
            .willReturn(Optional.of(Projekt.builder().tailoring(Tailoring.builder().name("master").build()).build()));

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/phase/{phase}", "SAMPLE", "master1")
            .contentType(APPLICATION_JSON)
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void addAnforderungDokument_StatusOkUndLinks() throws Exception {
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
        given(serviceMock.addAnforderungDokument("SAMPLE", "master", "DUMMY_CM.pdf", data))
            .willReturn(Optional.of(tailoring));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(tailoring)))
            .willReturn(TailoringResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(multipart("/projekt/{projekt}/tailoring/{tailoring}/dokument", "SAMPLE", "master")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/projekt/SAMPLE/tailoring/master/dokument"));

        verify(serviceMock, times(1)).addAnforderungDokument("SAMPLE", "master", "DUMMY_CM.pdf", data);
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(tailoring));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void addAnforderungDokument_ProjektUndPhaseNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        MockMultipartFile dokument = new MockMultipartFile("datei", "DUMMY_CM.pdf",
            "text/plain", data);

        given(serviceMock.addAnforderungDokument("SAMPLE", "master", "DUMMY_CM.pdf", data))
            .willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(multipart("/projekt/{projekt}/tailoring/{tailoring}/dokument", "SAMPLE", "master")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
            .accept("application/hal+json")
        );

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).addAnforderungDokument("SAMPLE", "master", "DUMMY_CM.pdf", data);
        verify(mapperMock, times(0)).toResource(any(), any(Tailoring.class));

        assertThatNoException();
    }

    @Test
    void getAnforderungDokument_ProjektUndPhaseVorhanden_DateiWirdZurueckGegeben() throws Exception {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(Paths.get("src/test/resources/screeningsheet_0d.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(serviceMock.createAnforderungDokument("SAMPLE", "master"))
            .willReturn(Optional.of(Datei.builder()
                .docId("DUMMY-RD-PS-DLR-1000-DV-8.2.1_01.01.2021_Product Assurance Safety Sustainability Requirements for DUMMY")
                .type("pdf")
                .bytes(data)
                .build()
            ));

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/dokument/katalog", "SAMPLE", "master")
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
    void getAnforderungDokument_ProjektUndPhaseNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.createAnforderungDokument("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/dokument", "SAMPLE", "master")
            .accept("application/zip")
        );

        // assert

        actual.andExpect(status().isNotFound());
        assertThatNoException();
    }

    @Test
    void getAnforderungen_ProjektUndPhaseUndKapitelVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        TailoringAnforderung anforderung = TailoringAnforderung.builder()
            .position("a")
            .ausgewaehlt(TRUE)
            .build();
        given(serviceMock.getAnforderungen("SAMPLE", "master", "1.1"))
            .willReturn(Optional.of(of(anforderung)));

        ArgumentCaptor<TailoringAnforderung> anforderungCaptor = forClass(TailoringAnforderung.class);
        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), anforderungCaptor.capture())).willReturn(TailoringAnforderungResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/katalog/{kapitel}/anforderung", "SAMPLE", "master", "1.1")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), anforderungCaptor.capture());
        assertThat(anforderungCaptor.getValue()).isEqualTo(anforderung);
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").kapitel("1.1").build());
    }

    @Test
    void getDokumentZeichnungen_ProjektUndPhaseVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        DokumentZeichnung zeichnung = DokumentZeichnung.builder()
            .bereich("Software")
            .build();
        given(serviceMock.getDokumentZeichnungen("SAMPLE", "master"))
            .willReturn(Optional.of(of(zeichnung)));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(zeichnung)))
            .willReturn(DokumentZeichnungResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/zeichnung", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).getDokumentZeichnungen("SAMPLE", "master");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(zeichnung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void updateDokumentZeichnung_ProjektUndPhaseVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        DokumentZeichnung zeichnung = DokumentZeichnung.builder()
            .bereich("Software")
            .build();
        given(serviceMock.updateDokumentZeichnung("SAMPLE", "master", zeichnung))
            .willReturn(Optional.of(zeichnung));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(zeichnung)))
            .willReturn(DokumentZeichnungResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/zeichnung/{bereich}", "SAMPLE", "master", "Software")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString(zeichnung))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).updateDokumentZeichnung("SAMPLE", "master", zeichnung);
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(zeichnung));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void updateName_AlleParameterVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        TailoringInformation projektPhase = TailoringInformation.builder()
            .name("test")
            .build();
        given(serviceMock.updateName("SAMPLE", "master", "test"))
            .willReturn(Optional.of(projektPhase));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), eq(projektPhase)))
            .willReturn(TailoringInformationResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/name", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString("test"))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8.displayName())
        );

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).updateName("SAMPLE", "master", "test");
        verify(mapperMock, times(1)).toResource(pathContextCaptor.capture(), eq(projektPhase));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("test").build());
    }

    @Test
    void updateName_PhaseNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.updateName("SAMPLE", "master", "test"))
            .willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(put("/projekt/{projekt}/tailoring/{tailoring}/name", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
            .content(objectMapper.writeValueAsString("test"))
            .contentType(APPLICATION_JSON)
            .characterEncoding(UTF_8.displayName()));

        // assert
        actual.andExpect(status().isPreconditionFailed());

        verify(serviceMock, times(1)).updateName("SAMPLE", "master", "test");
        verify(mapperMock, times(0)).toResource(any(), any(Tailoring.class));
    }


    @Test
    void getDokumentListe_ProjektUndPhaseVorhanden_StatusOKUndLinks() throws Exception {
        // arrange
        Dokument dokument1 = Dokument.builder().build();
        Dokument dokument2 = Dokument.builder().build();

        given(repositoryMock.getDokumentListe("SAMPLE", "master"))
            .willReturn(of(dokument1, dokument2));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), any(Dokument.class)))
            .willReturn(DokumentResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/zeichnung/doks", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).getDokumentListe("SAMPLE", "master");
        verify(mapperMock, times(2)).toResource(pathContextCaptor.capture(), any(Dokument.class));
        assertThat(pathContextCaptor.getValue().build()).isEqualTo(PathContext.builder().projekt("SAMPLE").tailoring("master").build());
    }

    @Test
    void getDokumentListe_ProjektUndPhaseNichtVorhanden_StatusOK() throws Exception {
        // arrange
        given(repositoryMock.getDokumentListe("SAMPLE", "master")).willReturn(emptyList());

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), any(Dokument.class)))
            .willReturn(DokumentResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/zeichnung/doks", "SAMPLE", "master")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).getDokumentListe("SAMPLE", "master");
        verify(mapperMock, times(0)).toResource(pathContextCaptor.capture(), any(Dokument.class));
    }

    @Test
    void getProfile_KeineProfileVorhanden_StatusOK() throws Exception {
        // arrange
        given(repositoryMock.getSelektionsVektorProfile()).willReturn(emptyList());

        // act
        ResultActions actual = mockMvc.perform(get("/selektionsvektor")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).getSelektionsVektorProfile();
        verify(mapperMock, times(0)).toResource(any(PathContextBuilder.class), any(Dokument.class));
    }

    @Test
    void getProfile_2ProfileVorhanden_StatusOK() throws Exception {
        // arrange
        given(repositoryMock.getSelektionsVektorProfile()).willReturn(asList(
            SelektionsVektorProfil.builder().build(),
            SelektionsVektorProfil.builder().build()
        ));

        ArgumentCaptor<PathContextBuilder> pathContextCaptor = forClass(PathContextBuilder.class);
        given(mapperMock.toResource(pathContextCaptor.capture(), any(SelektionsVektorProfil.class)))
            .willReturn(SelektionsVektorProfilResource.builder().build());

        // act
        ResultActions actual = mockMvc.perform(get("/selektionsvektor")
            .accept(HAL_JSON_VALUE)
        );

        // assert
        actual.andExpect(status().isOk());

        verify(repositoryMock, times(1)).getSelektionsVektorProfile();
        verify(mapperMock, times(2)).toResource(eq(pathContextCaptor.getValue()), any(SelektionsVektorProfil.class));
    }

    @Test
    void getVergleichsdokumentDokument_KeineVergleichsdokumentVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.createVergleichsDokument("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/dokument/vergleich", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).createVergleichsDokument("SAMPLE", "master");
    }

    @Test
    void getVergleichsdokumentDokument_VergleichsdokumentVorhanden_StatusOK() throws Exception {
        // arrange
        given(serviceMock.createVergleichsDokument("SAMPLE", "master")).willReturn(Optional.of(
            Datei.builder()
                .bytes("Blindtext".getBytes(UTF_8))
                .docId("DOCID_42")
                .type("pdf")
                .build())
        );

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/dokument/vergleich", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isOk());
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"DOCID_42\""));
        actual.andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));
        actual.andExpect(content().contentType("application/json"));

        verify(serviceMock, times(1)).createVergleichsDokument("SAMPLE", "master");
    }

    @Test
    void getDokument_KeinDokumentVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(repositoryMock.getDokument("SAMPLE", "master", "DOCID-42.pdf")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/dokument/{name}", "SAMPLE", "master", "DOCID-42.pdf"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(repositoryMock, times(1)).getDokument("SAMPLE", "master", "DOCID-42.pdf");
    }

    @Test
    void getDokument_DokumentVorhanden_StatusOK() throws Exception {
        // arrange
        given(repositoryMock.getDokument("SAMPLE", "master", "DOCID-42.pdf")).willReturn(Optional.of(
                Datei.builder()
                    .bytes("Blindtext".getBytes(UTF_8))
                    .docId("DOCID_42")
                    .type("pdf")
                    .build()
            )
        );

        // act
        ResultActions actual = mockMvc.perform(get("/projekt/{projekt}/tailoring/{tailoring}/dokument/{name}", "SAMPLE", "master", "DOCID-42.pdf"));

        // assert
        actual.andExpect(status().isOk());
        actual.andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"DOCID-42.pdf\""));
        actual.andExpect(header().string("Access-Control-Expose-Headers", "Content-Disposition"));
        actual.andExpect(content().contentType("application/json"));

        verify(repositoryMock, times(1)).getDokument("SAMPLE", "master", "DOCID-42.pdf");
    }

    @Test
    void deleteTailoring_TailoringNichtVorhanden_StatusNotFound() throws Exception {
        // arrange
        given(serviceMock.deleteTailoring("SAMPLE", "master")).willReturn(empty());

        // act
        ResultActions actual = mockMvc.perform(delete("/projekt/{projekt}/tailoring/{tailoring}", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isNotFound());

        verify(serviceMock, times(1)).deleteTailoring("SAMPLE", "master");
    }

    @Test
    void deleteTailoring_TailoringVorhanden_StatusOK() throws Exception {
        // arrange
        given(serviceMock.deleteTailoring("SAMPLE", "master")).willReturn(Optional.of(TRUE));

        // act
        ResultActions actual = mockMvc.perform(delete("/projekt/{projekt}/tailoring/{tailoring}", "SAMPLE", "master"));

        // assert
        actual.andExpect(status().isOk());

        verify(serviceMock, times(1)).deleteTailoring("SAMPLE", "master");
    }

    @Test
    void updateAnforderungen_DateiLeer_StatusAccepted() throws Exception {
        // arrange
        MockMultipartFile dokument = new MockMultipartFile("datei", "DUMMY_CM.pdf",
            "text/plain", (byte[]) null);
        // act
        ResultActions actual = mockMvc.perform(multipart("/projekt/{projekt}/tailoring/{tailoring}/anforderungen/import", "SAMPLE", "master")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
        );

        // assert
        actual.andExpect(status().isAccepted());

        verify(serviceMock, times(1)).updateAusgewaehlteAnforderungen("SAMPLE", "master", new byte[0]);
    }

    @Test
    void updateAnforderungen_DateiNichtLeer_StatusAccepted() throws Exception {
        // arrange
        MockMultipartFile dokument = new MockMultipartFile("datei", "DUMMY_CM.pdf",
            "text/plain", "Excel Import Datei".getBytes(UTF_8));
        // act
        ResultActions actual = mockMvc.perform(multipart("/projekt/{projekt}/tailoring/{tailoring}/anforderungen/import", "SAMPLE", "master")
            .file(dokument)
            .contentType(MULTIPART_FORM_DATA)
        );

        // assert
        actual.andExpect(status().isAccepted());

        verify(serviceMock, times(1)).updateAusgewaehlteAnforderungen("SAMPLE", "master", "Excel Import Datei".getBytes(UTF_8));
    }

//    @PostMapping(value = PROJEKTPHASENANFORDERUNG)
//    public ResponseEntity<EntityModel<ScreeningSheetResource>> createScreeningSheet(
//        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
//        @Parameter(description = "Identifikator der Phase") @PathVariable("phase") String phase,
//        @RequestPart(value = "datei") MultipartFile datei) {
//
//        try {
//            projektPhaseService.updateAusgewaehlteAnforderungen(projekt, phase, datei.getBytes());
//            return ResponseEntity.accepted().build();
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//
//    }
}
