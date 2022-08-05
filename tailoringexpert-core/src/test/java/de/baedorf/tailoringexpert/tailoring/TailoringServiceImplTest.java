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

import de.baedorf.tailoringexpert.anforderung.AnforderungService;
import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Dokument;
import de.baedorf.tailoringexpert.domain.DokumentZeichnung;
import de.baedorf.tailoringexpert.domain.DokumentZeichnungStatus;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import de.baedorf.tailoringexpert.domain.Projekt;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetParameter;
import de.baedorf.tailoringexpert.domain.SelektionsVektor;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.domain.TailoringInformation;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static de.baedorf.tailoringexpert.domain.Phase.E;
import static de.baedorf.tailoringexpert.domain.Phase.F;
import static de.baedorf.tailoringexpert.domain.TailoringStatus.AKTIV;

import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
class TailoringServiceImplTest {

    private TailoringServiceImpl service;

    private TailoringServiceRepository repositoryMock;
    private TailoringServiceMapper mapperMock;
    private DokumentService dokumentServiceMock;
    private AnforderungService anforderungServiceMock;
    private Function<byte[], Map<String, Collection<ImportAnforderung>>> tailoringAnforderungFileReaderMock;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(TailoringServiceRepository.class);
        this.mapperMock = mock(TailoringServiceMapper.class);
        this.dokumentServiceMock = mock(DokumentService.class);
        this.anforderungServiceMock = mock(AnforderungService.class);
        this.tailoringAnforderungFileReaderMock = mock(Function.class);
        this.service = new TailoringServiceImpl(
            repositoryMock,
            mapperMock,
            dokumentServiceMock,
            anforderungServiceMock,
            tailoringAnforderungFileReaderMock
        );
    }

    @Test
    void addAnforderungDokument_ProjektNichtVorhanden_DokumentWirdNichtHinzugefuegt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }
        given(repositoryMock.getProjekt("DUMMY")).willReturn(empty());

        // act
        Optional<Tailoring> actual = service.addAnforderungDokument("DUMMY", "master", "dummy.pdf", data);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void addAnforderungDokument_ProjektPhaseNichtVorhanden_DokumentWirdNichtHinzugefuegt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProjekt("DUMMY")).willReturn(of(
            Projekt.builder()
                .tailorings(Collections.emptyList())
                .build())
        );

        // act
        Optional<Tailoring> actual = service.addAnforderungDokument("DUMMY", "master", "dummy.pdf", data);

        // assert
        assertThat(actual).isEmpty();
    }


    @Test
    void addAnforderungDokument_ProjektPhaseVorhanden_DokumentHinzugefuegt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getTailoring("SAMPLE", "master"))
            .willAnswer(invocation -> of(
                of(Tailoring.builder()
                    .name("master")
                    .build()
                ))
            );

        given(repositoryMock.updateAnforderungDokument(eq("SAMPLE"), eq("master"), any()))
            .willAnswer(invocation ->
                of(Tailoring.builder()
                    .dokumente(asList(
                        Dokument.builder()
                            .name(((Dokument) invocation.getArgument(2)).getName())
                            .build()))
                    .build())
            );


        // act
        Optional<Tailoring> actual = service.addAnforderungDokument("SAMPLE", "master", "dummy.pdf", data);

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getDokumente()).hasSize(1);
        assertThat(actual.get().getDokumente().iterator().next().getName()).isEqualTo("dummy.pdf");
    }

    @Test
    void addAnforderungDokument_ProjektPhaseNichtVorhanden_DokumentNichtHinzugefuegt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getTailoring("SAMPLE", "master"))
            .willReturn(empty());

        // act
        Optional<Tailoring> actual = service.addAnforderungDokument("SAMPLE", "master", "dummy.pdf", data);

        // assert
        verify(repositoryMock, times(0)).updateAnforderungDokument(anyString(), anyString(), any());
        assertThat(actual).isEmpty();
    }

    @Test
    void getKatalog_ProjektNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getKatalog(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKatalog_PhasetNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getKatalog("DUMMY", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKatalog_PhaseVorhanden_KatalogGefunden() {
        // arrange
        given(repositoryMock.getTailoring("SAMPLE", "master"))
            .willAnswer(invocation -> of(
                Tailoring.builder()
                    .name("master")
                    .katalog(Katalog.<TailoringAnforderung>builder().build())
                    .build())
            );

        // act
        Optional<Katalog<TailoringAnforderung>> actual = service.getKatalog("SAMPLE", "master");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void getKatalog_ProjektNichtVorhanden_KatalogEmpty() {
        // arrange
        given(repositoryMock.getProjekt("SAMPLE"))
            .willAnswer(invocation -> empty());

        // act
        Optional<Katalog<TailoringAnforderung>> actual = service.getKatalog("SAMPLE", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getKatalog_PhaseNichtVorhanden_KatalogEmpty() {
        // arrange
        given(repositoryMock.getProjekt("SAMPLE"))
            .willAnswer(invocation -> of(
                Projekt.builder()
                    .kuerzel(invocation.getArgument(0))
                    .tailorings(asList(
                        Tailoring.builder()
                            .name("master1")
                            .katalog(Katalog.<TailoringAnforderung>builder().build())
                            .build()
                    ))
                    .build())
            );

        // act
        Optional<Katalog<TailoringAnforderung>> actual = service.getKatalog("SAMPLE", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void createAnforderungDokument_PhaseNichtVorhanden_KeinDokumentErstellt() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "master25")).willReturn(empty());

        // act
        Optional<Datei> actual = service.createAnforderungDokument("DUMMY", "master25");

        // assert
        assertThat(actual).isEmpty();
        verify(dokumentServiceMock, times(0)).createAnforderungDokument(any(), any());
    }

    @Test
    void createAnforderungDokument() {
        // arrange
        Collection<DokumentZeichnung> zeichnungen = asList(
            DokumentZeichnung.builder()
                .status(DokumentZeichnungStatus.PREPARED)
                .bereich("Safety")
                .unterzeichner("B. Safe")
                .build(),
            DokumentZeichnung.builder()
                .status(DokumentZeichnungStatus.AGREED)
                .bereich("Software")
                .unterzeichner("Software Tuppes")
                .build(),
            DokumentZeichnung.builder()
                .status(DokumentZeichnungStatus.AGREED)
                .bereich("Project Management")
                .unterzeichner("P. Management/RD-???")
                .build(),
            DokumentZeichnung.builder()
                .status(DokumentZeichnungStatus.RELEASED)
                .bereich("Head of Product Assurance")
                .unterzeichner("Head Hunter/RD-PS")
                .build()
        );

        Katalog<TailoringAnforderung> katalog = Katalog.<TailoringAnforderung>builder()
            .version("8.2.1")
            .build();

        Tailoring tailoring = Tailoring.builder()
            .name("master1")
            .katalog(katalog)
            .screeningSheet(ScreeningSheet.builder()
                .parameters(asList(ScreeningSheetParameter.builder().bezeichnung(ScreeningSheetDataProviderSupplier.Kuerzel.getName()).wert("SAMPLE").build()))
                .build())
            .zeichnungen(zeichnungen)
            .build();
        given(repositoryMock.getTailoring("SAMPLE", "master1"))
            .willAnswer(invocation -> of(tailoring));

        given(dokumentServiceMock.createAnforderungDokument(eq(tailoring), any()))
            .willReturn(of(Datei.builder().build()));

        // act
        Optional<Datei> actual = service.createAnforderungDokument("SAMPLE", "master1");

        // assert
        assertThat(actual).isPresent();
        verify(dokumentServiceMock, times(1))
            .createAnforderungDokument(eq(tailoring), any());
    }

    @Test
    void getAnforderungen_NullProjekt_ExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getAnforderungen(null, "master", "1.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderungen_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getAnforderungen("Dummy", null, "1.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderungen_NullKapitel_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getAnforderungen("Dummy", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderungen_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TailoringService serviceSpy = spy(service);
        doReturn(empty()).when(serviceSpy).getKapitel(anyString(), anyString(), any());

        // act
        Optional<List<TailoringAnforderung>> actual = serviceSpy.getAnforderungen("Dummy", "master1", "1.1");

        // assert
        assertThat(actual).isEmpty();
        verify(serviceSpy, times(1)).getKapitel("Dummy", "master1", "1.1");
    }

    @Test
    void getAnforderungen_KapitelNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TailoringService serviceSpy = spy(service);
        doReturn(empty()).when(serviceSpy).getKapitel(anyString(), anyString(), any());

        // act
        Optional<List<TailoringAnforderung>> actual = serviceSpy.getAnforderungen("Dummy", "master", "1.1");

        // assert
        assertThat(actual).isEmpty();
        verify(serviceSpy, times(1)).getKapitel("Dummy", "master", "1.1");
    }

    @Test
    void getAnforderungen_KapitelVorhanden_AnforderungenWerdenZurueckGegeben() {
        // arrange
        TailoringService serviceSpy = spy(service);
        doReturn(of(
            Kapitel.<TailoringAnforderung>builder()
                .nummer("1.1")
                .anforderungen(asList(
                    TailoringAnforderung.builder()
                        .text("Anforderung 1")
                        .build(),
                    TailoringAnforderung.builder()
                        .text("Anforderung 2")
                        .build()
                ))
                .build()))
            .when(serviceSpy).getKapitel("Dummy", "master", "1.1");

        // act
        Optional<List<TailoringAnforderung>> actual = serviceSpy.getAnforderungen("Dummy", "master", "1.1");

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get()).hasSize(2);
        verify(serviceSpy, times(1)).getKapitel("Dummy", "master", "1.1");
    }

    @Test
    void getScreeningSheet_NullProjekt_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getScreeningSheet(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getScreeningSheet_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getScreeningSheet("Dummy", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getScreeningSheet_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(empty());

        // act
        Optional<ScreeningSheet> actual = service.getScreeningSheet("Dummy", "master1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master1");
    }

    @Test
    void getScreeningSheet_PhaseOhneScreeningSheetVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(of(
            Tailoring.builder().screeningSheet(null).build()
        ));

        // act
        Optional<ScreeningSheet> actual = service.getScreeningSheet("Dummy", "master");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master");
    }

    @Test
    void getScreeningSheet_PhaseMitScreeningSheetVorhanden_ScreeningSheetWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(of(
            Tailoring.builder().screeningSheet(ScreeningSheet.builder().build()).build()
        ));

        // act
        Optional<ScreeningSheet> actual = service.getScreeningSheet("Dummy", "master");

        // assert
        assertThat(actual).isNotEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master");
    }

    @Test
    void getSelektionsVektor_NullProjekt_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getSelektionsVektor(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getSelektionsVektor_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getSelektionsVektor("Dummy", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getSelektionsVektor_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(empty());

        // act
        Optional<SelektionsVektor> actual = service.getSelektionsVektor("Dummy", "master1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master1");
    }

    @Test
    void getSelektionsVektor_PhaseMitSelektionsVektorVorhanden_SelektionsVektorWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(of(
            Tailoring.builder().selektionsVektor(SelektionsVektor.builder().build()).build()
        ));

        // act
        Optional<SelektionsVektor> actual = service.getSelektionsVektor("Dummy", "master");

        // assert
        assertThat(actual).isNotEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master");
    }


    @Test
    void getKapitel_NullProjekt_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getKapitel(null, "master", "1.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getKapitel("Dummy", null, "1.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_NullKapitel_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getKapitel("Dummy", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "master")).willReturn(empty());

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = service.getKapitel("DUMMY", "master", "1.1");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getKapitel_KapitelVorhanden_KapitelZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring("Dummy", "master")).willReturn(of(
            Tailoring.builder()
                .katalog(Katalog.<TailoringAnforderung>builder()
                    .toc(Kapitel.<TailoringAnforderung>builder()
                        .kapitel(asList(
                            Kapitel.<TailoringAnforderung>builder()
                                .nummer("1")
                                .kapitel(asList(
                                    Kapitel.<TailoringAnforderung>builder()
                                        .nummer("1.1")
                                        .build()
                                ))
                                .build()
                        ))
                        .build())
                    .build())
                .build()));

        // act
        Optional<Kapitel<TailoringAnforderung>> actual = service.getKapitel("Dummy", "master", "1.1");

        // assert
        assertThat(actual).isPresent();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master");
    }

    @Test
    void getDokumentZeichnungen_NullProjekt_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getDokumentZeichnungen(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDokumentZeichnungen_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getDokumentZeichnungen("Dummy", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDokumentZeichnungen_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(empty());

        // act
        Optional<Collection<DokumentZeichnung>> actual = service.getDokumentZeichnungen("Dummy", "master1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master1");
    }

    @Test
    void getDokumentZeichnungen_PhaseMitDokumentZeichnungVorhanden_DokumentZeichnungWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(of(
            Tailoring.builder().zeichnungen(asList(
                    DokumentZeichnung.builder()
                        .bereich("Software")
                        .unterzeichner("Hans Dampf")
                        .status(DokumentZeichnungStatus.AGREED)
                        .build()
                ))
                .build()
        ));

        // act
        Optional<Collection<DokumentZeichnung>> actual = service.getDokumentZeichnungen("Dummy", "master");

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).hasSize(1);
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master");
    }

    @Test
    void updateDokumentZeichnung_NullProjekt_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.updateDokumentZeichnung(null, "master", DokumentZeichnung.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateDokumentZeichnung_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.updateDokumentZeichnung("Dummy", null, DokumentZeichnung.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateDokumentZeichnung_NullZeichnung_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.updateDokumentZeichnung("Dummy", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateDokumentZeichnung_ZeichnungNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        DokumentZeichnung zeichnung = DokumentZeichnung.builder()
            .bereich("Software")
            .unterzeichner("Hans Dampf")
            .status(DokumentZeichnungStatus.AGREED)
            .build();

        given(repositoryMock.updateDokumentZeichnung(anyString(), anyString(), any(DokumentZeichnung.class))).willReturn(empty());
        // act
        Optional<DokumentZeichnung> actual = service.updateDokumentZeichnung("Dummy", "master", zeichnung);

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).updateDokumentZeichnung("Dummy", "master", zeichnung);
    }

    @Test
    void updateDokumentZeichnung_ZeichnungVorhanden_AktualisierteZeichnungWirdZurueckGegeben() {
        // arrange
        DokumentZeichnung zeichnung = DokumentZeichnung.builder()
            .bereich("Software")
            .unterzeichner("Hans Dampf")
            .status(DokumentZeichnungStatus.AGREED)
            .build();

        given(repositoryMock.updateDokumentZeichnung("Dummy", "master", zeichnung)).willReturn(of(zeichnung));
        // act
        Optional<DokumentZeichnung> actual = service.updateDokumentZeichnung("Dummy", "master", zeichnung);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .contains(zeichnung);
        verify(repositoryMock, times(1)).updateDokumentZeichnung("Dummy", "master", zeichnung);
    }

    @Test
    void updateName_NeuerNameNichtVorhanden_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.updateName("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateName_NeuerNameBereitsVerwendet_NameWirdNichtAktualisiert() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "test"))
            .willReturn(of(Tailoring.builder().build()));

        // act
        Optional<TailoringInformation> actual = service.updateName("DUMMY", "master", "test");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0)).updateName(anyString(), anyString(), anyString());
    }

    @Test
    void updateName_NeuerNameNochNichtVerwendet_NameWirdAktualisiert() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "test"))
            .willReturn(empty());

        Tailoring tailoring = Tailoring.builder().build();
        given(repositoryMock.updateName("DUMMY", "master", "test"))
            .willReturn(of(tailoring));
        given(mapperMock.toTailoringInformation(tailoring))
            .willReturn(TailoringInformation.builder().build());

        // act
        Optional<TailoringInformation> actual = service.updateName("DUMMY", "master", "test");

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1)).toTailoringInformation(tailoring);
        verify(repositoryMock, times(1)).updateName("DUMMY", "master", "test");
    }

    @Test
    void updateName_PhaseUndNeuerNameGleich_NameWirdNichtAktualisiert() {
        // arrange

        // act
        Optional<TailoringInformation> actual = service.updateName("DUMMY", "master", "master");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0)).getTailoring(anyString(), anyString());
        verify(repositoryMock, times(0)).updateName(anyString(), anyString(), anyString());
    }

    @Test
    void createProjektPhase_ProjektPhaseWirdErzeugt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .data(data)
            .parameters(asList(ScreeningSheetParameter.builder().bezeichnung(ScreeningSheetDataProviderSupplier.Phase.getName()).wert(asList(E, F)).build()))
            .selektionsVektor(SelektionsVektor.builder().build())
            .build();

        SelektionsVektor anzuwendenderSelektionsVektor = SelektionsVektor.builder().build();

        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .toc(Kapitel.<KatalogAnforderung>builder()
                .kapitel(asList(
                    Kapitel.<KatalogAnforderung>builder()
                        .nummer("1")
                        .kapitel(asList(
                            Kapitel.<KatalogAnforderung>builder()
                                .nummer("1.1")
                                .build()
                        ))
                        .build()
                ))
                .build())
            .build();

        List<DokumentZeichnung> defaultZeichnungen = Collections.emptyList();
        given(repositoryMock.getDefaultZeichnungen()).willReturn(defaultZeichnungen);

        given(mapperMock.toTailoringKatalog(katalog, screeningSheet, anzuwendenderSelektionsVektor)).willReturn(Katalog.<TailoringAnforderung>builder().build());

        // act
        Tailoring actual = service.createTailoring("master1", "1000", screeningSheet, anzuwendenderSelektionsVektor, katalog);

        // assert
        assertThat(actual.getName()).isEqualTo("master1");
        assertThat(actual.getScreeningSheet()).isEqualTo(screeningSheet);
        assertThat(actual.getSelektionsVektor()).isEqualTo(anzuwendenderSelektionsVektor);
        assertThat(actual.getKatalog()).isNotNull();
        assertThat(actual.getZeichnungen()).isEqualTo(defaultZeichnungen);
        assertThat(actual.getStatus()).isEqualTo(AKTIV);
        assertThat(actual.getPhasen()).containsOnly(E, F);

        verify(mapperMock, times(1)).toTailoringKatalog(katalog, screeningSheet, anzuwendenderSelektionsVektor);
        verify(repositoryMock, times(1)).getDefaultZeichnungen();
    }


    @Test
    void createDokumente_ProjektNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.createDokumente(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDokumente_PhaseNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.createDokumente("DUMMY", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDokumente_PhaseNichtVorhanden_EmptyWirdZurueckgegeben() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "master1")).willReturn(empty());

        // act
        Optional<Datei> actual = service.createDokumente("DUMMY", "master1");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void createDokumente_ProjektUndPhaseVorhanden_ZipZurueckegeben() throws IOException {
        // arrange
        Tailoring tailoring = Tailoring.builder().name("master").build();
        given(repositoryMock.getTailoring("DUMMY", "master")).willReturn(of(tailoring));

        List<Datei> dokumente = asList(
            Datei.builder()
                .docId("DUMMY-KATALOG")
                .bytes("Testdokument".getBytes(UTF_8))
                .type("pdf")
                .build()
        );
        given(dokumentServiceMock.createAll(eq(tailoring), any())).willReturn(dokumente);

        // act
        Optional<Datei> actual = service.createDokumente("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getDocId()).isEqualTo("DUMMY-master");
        assertThat(actual.get().getType()).isEqualTo("zip");

        Collection<String> zipDateien = dateiNamenImZip(actual.get().getBytes());
        assertThat(zipDateien)
            .hasSize(1)
            .containsExactly("DUMMY-KATALOG.pdf");
    }

    @Test
    void createVergleichsDokument_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "master")).willReturn(empty());

        // act
        Optional<Datei> actual = service.createVergleichsDokument("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void createVergleichsDokument_PhaseVorhanden_VergeleichsdokuemnWirdZurueckGegeben() {
        // arrange
        Tailoring tailoring = Tailoring.builder().build();
        given(repositoryMock.getTailoring("DUMMY", "master")).willReturn(of(tailoring));

        given(dokumentServiceMock.createVergleichsDokument(eq(tailoring), any())).willReturn(of(Datei.builder().build()));

        // act
        Optional<Datei> actual = service.createVergleichsDokument("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
        verify(dokumentServiceMock, times(1)).createVergleichsDokument(eq(tailoring), any());
    }


    @Test
    void updateAusgewaehlteAnforderungen_ProjektNull_NullPointerExceptionWirdGeworfen() {
        // arrange
        String project = null;
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer Datei".getBytes(UTF_8);

        // act
        Throwable actual = catchThrowable(() -> service.updateAusgewaehlteAnforderungen(project, phase, data));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAusgewaehlteAnforderungen_PhaseNull_NullPointerExceptionWirdGeworfen() throws IOException {
        // arrange
        String project = "DUMMY";
        String phase = null;
        byte[] data = "Filereader wird gemockt. Kein parsen einer Datei".getBytes(UTF_8);

        // act
        Throwable actual = catchThrowable(() -> service.updateAusgewaehlteAnforderungen(project, phase, data));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAusgewaehlteAnforderungen_DataNull_DateiWirdNichtEingelesenVoidReturn() {
        // arrange
        String project = "DUMMY";
        String phase = "master";

        // act
        service.updateAusgewaehlteAnforderungen(project, phase, null);

        // assert
        verify(tailoringAnforderungFileReaderMock, times(0)).apply(any());
    }

    @Test
    void updateAusgewaehlteAnforderungen_AnforderungMitUngueltigemStatus_AnforderungMitUngueltigemStatusNichtVerarbeitet() {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer Datei".getBytes(UTF_8);

        given(tailoringAnforderungFileReaderMock.apply(data)).willReturn(Map.ofEntries(
                new AbstractMap.SimpleEntry<>("1", asList(
                    ImportAnforderung.builder().position("a").anwendbar("JEIN").build(),
                    ImportAnforderung.builder().position("b").anwendbar("NEIN").build()
                ))
            )
        );

        // act
        service.updateAusgewaehlteAnforderungen(project, phase, data);

        // assert
        verify(anforderungServiceMock, times(0)).handleAusgewaehlt(eq("DUMMY"), eq("master"), eq("1"), eq("a"), any());
        verify(anforderungServiceMock, times(1)).handleAusgewaehlt("DUMMY", "master", "1", "b", false);
    }

    @Test
    void updateAusgewaehlteAnforderungen_AnforderungenMitGueltigemStatus_AnforderungenWerdenVerarbeitet() {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer Datei".getBytes(UTF_8);

        given(tailoringAnforderungFileReaderMock.apply(data)).willReturn(Map.ofEntries(
                new AbstractMap.SimpleEntry<>("1", asList(
                    ImportAnforderung.builder().position("a").anwendbar("JA").build(),
                    ImportAnforderung.builder().position("b").anwendbar("NEIN").build()
                ))
            )
        );

        // act
        service.updateAusgewaehlteAnforderungen(project, phase, data);

        // assert
        verify(anforderungServiceMock, times(1)).handleAusgewaehlt("DUMMY", "master", "1", "a", true);
        verify(anforderungServiceMock, times(1)).handleAusgewaehlt("DUMMY", "master", "1", "b", false);
    }


    @Test
    void updateAusgewaehlteAnforderungen_AnforderungenMitGueltigemStatusUndTextanpassung_AnforderungenWerdenVerarbeitet() {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer Datei".getBytes(UTF_8);

        given(tailoringAnforderungFileReaderMock.apply(data)).willReturn(Map.ofEntries(
                new AbstractMap.SimpleEntry<>("1", asList(
                    ImportAnforderung.builder().position("a").anwendbar("JA").text("Dies ist der neue Text").build(),
                    ImportAnforderung.builder().position("b").anwendbar("NEIN").build()
                ))
            )
        );

        // act
        service.updateAusgewaehlteAnforderungen(project, phase, data);

        // assert
        verify(anforderungServiceMock, times(1)).handleText(eq("DUMMY"), eq("master"), eq("1"), any(), any());
        verify(anforderungServiceMock, times(1)).handleText("DUMMY", "master", "1", "a", "Dies ist der neue Text");

    }

    @Test
    void updateAusgewaehlteAnforderungen_AnforderungenMitGueltigemStatusUndLeererTextanpassung_AnforderungenOhneTextWerdenVerarbeitet() {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer Datei".getBytes(UTF_8);

        given(tailoringAnforderungFileReaderMock.apply(data)).willReturn(Map.ofEntries(
                new AbstractMap.SimpleEntry<>("1", asList(
                    ImportAnforderung.builder().position("a").anwendbar("JA").text("").build(),
                    ImportAnforderung.builder().position("b").anwendbar("NEIN").build()
                ))
            )
        );

        // act
        service.updateAusgewaehlteAnforderungen(project, phase, data);

        // assert
        verify(anforderungServiceMock, times(0)).handleText(eq("DUMMY"), eq("master"), eq("1"), any(), any());

    }


    @Test
    void deleteProjektPhase_ProjektNull_NullPointerExceptionWirdGeworfen() throws IOException {
        // arrange
        String project = null;
        String phase = null;

        // act
        Throwable actual = catchThrowable(() -> service.deleteTailoring(project, phase));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteProjektPhase_PhaseNull_NullPointerExceptionWirdGeworfen() throws IOException {
        // arrange
        String project = "DUMMY";
        String phase = null;

        // act
        Throwable actual = catchThrowable(() -> service.deleteTailoring(project, phase));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void deleteProjektPhase_ProjektPhaseNichtVorhanden_KeineVerarbeitungEmptyWirdZurueckGegeben() throws IOException {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        given(repositoryMock.getTailoring(project, phase)).willReturn(empty());

        // act
        Optional<Boolean> actual = service.deleteTailoring(project, phase);

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getTailoring(project, phase);
    }

    @Test
    void deleteProjektPhase_ProjektPhaseVorhanden_TrueWirdZurueckGegeben() throws IOException {
        // arrange
        String project = "DUMMY";
        String phase = "master";

        given(repositoryMock.getTailoring(project, phase)).willReturn(of(Tailoring.builder().build()));
        given(repositoryMock.deleteTailoring(project, phase)).willReturn(TRUE);

        // act
        Optional<Boolean> actual = service.deleteTailoring(project, phase);

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isTrue();
        verify(repositoryMock, times(1)).getTailoring(project, phase);
        verify(repositoryMock, times(1)).deleteTailoring(project, phase);
    }


    Collection<String> dateiNamenImZip(byte[] zip) throws IOException {
        Collection<String> result = new ArrayList<>();
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zip));
        ZipEntry entry;
        while (nonNull(entry = zin.getNextEntry())) {
            result.add(entry.getName());
            zin.closeEntry();
        }
        zin.close();
        return result;
    }
}
