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

import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Dokument;
import de.baedorf.tailoringexpert.domain.DokumentEntity;
import de.baedorf.tailoringexpert.domain.DokumentZeichnerEntity;
import de.baedorf.tailoringexpert.domain.DokumentZeichnung;
import de.baedorf.tailoringexpert.domain.DokumentZeichnungEntity;
import de.baedorf.tailoringexpert.domain.DokumentZeichnungStatus;
import de.baedorf.tailoringexpert.domain.Projekt;
import de.baedorf.tailoringexpert.domain.ProjektEntity;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetEntity;
import de.baedorf.tailoringexpert.domain.SelektionsVektorProfil;
import de.baedorf.tailoringexpert.domain.SelektionsVektorProfilEntity;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringEntity;
import de.baedorf.tailoringexpert.repository.DokumentZeichnerRepository;
import de.baedorf.tailoringexpert.repository.ProjektRepository;
import de.baedorf.tailoringexpert.repository.SelektionsVektorProfilRepository;
import de.baedorf.tailoringexpert.repository.TailoringRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPATailoringServiceRepositoryTest {

    JPATailoringServiceRepositoryMapper mapperMock;
    ProjektRepository projektRepositoryMock;
    TailoringRepository tailoringRepositoryMock;
    SelektionsVektorProfilRepository selektionsVektorProfilRepositoryMock;
    DokumentZeichnerRepository dokumentZeichnerRepositoryMock;
    JPATailoringServiceRepository repository;

    @BeforeEach
    void setup() {
        this.mapperMock = mock(JPATailoringServiceRepositoryMapper.class);
        this.projektRepositoryMock = mock(ProjektRepository.class);
        this.tailoringRepositoryMock = mock(TailoringRepository.class);
        this.selektionsVektorProfilRepositoryMock = mock(SelektionsVektorProfilRepository.class);
        this.dokumentZeichnerRepositoryMock = mock(DokumentZeichnerRepository.class);
        this.repository = new JPATailoringServiceRepository(
            mapperMock,
            projektRepositoryMock,
            tailoringRepositoryMock,
            selektionsVektorProfilRepositoryMock,
            dokumentZeichnerRepositoryMock
        );
    }

    @Test
    void getProjekt_ProjektNichtVorhanden_EmptyErgebnis() {
        // arrange
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(null);

        // act
        Optional<Projekt> actual = repository.getProjekt("SAMPLE");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getProjekt_ProjektVorhanden_ProjektWirdZurueckGegeben() {
        // arrange
        ProjektEntity projekt = ProjektEntity.builder().build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        given(mapperMock.toDomain(projekt))
            .willReturn(Projekt.builder().build());

        // act
        Optional<Projekt> actual = repository.getProjekt("SAMPLE");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void updateKatalog_ProjektPhaseNichtVorhanden_EingabePhaseAlsErgebnis() {
        // arrange
        TailoringEntity projektPhaseToUpdate = TailoringEntity.builder()
            .name("master1")
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .tailorings(asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                projektPhaseToUpdate
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        Tailoring tailoring = Tailoring.builder()
            .name("master2")
            .build();

        // act
        Tailoring actual = repository.updateTailoring("SAMPLE", tailoring);

        // assert
        assertThat(actual).isNotNull();
        verify(mapperMock, times(0))
            .addKatalog(tailoring, projektPhaseToUpdate);
    }

    @Test
    void updateKatalog_ProjektPhaseVorhanden_ProjektPhaseAktualisiert() {
        // arrange
        TailoringEntity projektPhaseToUpdate = TailoringEntity.builder()
            .name("master1")
            .build();
        ProjektEntity projekt = ProjektEntity.builder()
            .tailorings(asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                projektPhaseToUpdate
            ))
            .build();
        given(projektRepositoryMock.findByKuerzel("SAMPLE"))
            .willReturn(projekt);

        Tailoring tailoring = Tailoring.builder()
            .name("master1")
            .build();

        given(mapperMock.toDomain(projektPhaseToUpdate))
            .willReturn(tailoring);

        // act
        Tailoring actual = repository.updateTailoring("SAMPLE", tailoring);

        // assert
        assertThat(actual).isNotNull();
        verify(mapperMock, times(1))
            .addKatalog(tailoring, projektPhaseToUpdate);
    }

    @Test
    void updateAnforderungDokument() {
        // arrange
        TailoringEntity projektPhaseToUpdate = TailoringEntity.builder()
            .name("master")
            .dokumente(new HashSet<>())
            .build();
        given(projektRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(projektPhaseToUpdate);

        Dokument dokument = Dokument.builder().build();
        Tailoring tailoring = Tailoring.builder()
            .name("master")
            .dokumente(asList(dokument))
            .build();

        given(mapperMock.toDomain(projektPhaseToUpdate))
            .willReturn(tailoring);

        // act
        Optional<Tailoring> actual = repository.updateAnforderungDokument("SAMPLE", "master", dokument);

        // assert
        assertThat(actual).isPresent();
        assertThat(projektPhaseToUpdate.getDokumente()).isNotEmpty();
    }


    @Test
    void updateAnforderungDokument_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(null);

        Dokument dokument = Dokument.builder().build();

        // act
        Optional<Tailoring> actual = repository.updateAnforderungDokument("SAMPLE", "master", dokument);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateName_NullProjekt_NameWirdNichtAktualisiert() {
        // arrange
        given(projektRepositoryMock.findTailoring(null, "master"))
            .willReturn(null);

        // act
        Optional<Tailoring> actual = repository.updateName(null, "master1", "test");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateName_NullPhase_NameWirdNichtAktualisiert() {
        // arrange
        given(projektRepositoryMock.findTailoring("DUMMY", null))
            .willReturn(null);

        // act
        Optional<Tailoring> actual = repository.updateName("DUMMY", null, "test");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateName_AlleParameterVorhanden_NameWirdAktualisiert() {
        // arrange
        TailoringEntity projektPhase = new TailoringEntity();
        given(projektRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(projektPhase);

        given(mapperMock.toDomain(projektPhase))
            .willAnswer(invocation -> Tailoring.builder()
                .name(((TailoringEntity) invocation.getArgument(0)).getName())
                .build());

        // act
        Optional<Tailoring> actual = repository.updateName("DUMMY", "master", "test");

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getName()).isEqualTo("test");
    }

    @Test
    void updateDokumentZeichnung_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<DokumentZeichnung> actual = repository.updateDokumentZeichnung("DUMMY", "master", DokumentZeichnung.builder().build());

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateDokumentZeichnung_ZeichnungBereichNichtVorhanden_EmptyWirdZurueckGegeben1() {
        // arrange
        DokumentZeichnungEntity zeichnungEntity = DokumentZeichnungEntity.builder()
            .status(DokumentZeichnungStatus.AGREED)
            .bereich("SW")
            .unterzeichner("Hans Dampf")
            .anwendbar(true)
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(TailoringEntity.builder()
                .zeichnungen(asList(
                    zeichnungEntity
                ))
                .build()
            );

        given(mapperMock.toDomain(zeichnungEntity)).willReturn(DokumentZeichnung.builder().build());
        DokumentZeichnung zeichnung = DokumentZeichnung.builder()
            .status(DokumentZeichnungStatus.RELEASED)
            .bereich("Safety")
            .unterzeichner("Hans Dampf")
            .anwendbar(false)
            .build();

        // act
        Optional<DokumentZeichnung> actual = repository.updateDokumentZeichnung("DUMMY", "master", zeichnung);

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(0)).updateDokumentZeichnung(any(), any());
        verify(mapperMock, times(0)).toDomain(any(DokumentZeichnungEntity.class));
    }

    @Test
    void updateDokumentZeichnung_ZeichnungVorhanden_NeueZeichnungWirdZurueckGegeben() {
        // arrange
        DokumentZeichnungEntity zeichnungEntity = DokumentZeichnungEntity.builder()
            .status(DokumentZeichnungStatus.AGREED)
            .bereich("SW")
            .unterzeichner("Hans Dampf")
            .anwendbar(true)
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(TailoringEntity.builder()
                .zeichnungen(asList(
                    zeichnungEntity
                ))
                .build()
            );

        given(mapperMock.toDomain(zeichnungEntity)).willReturn(DokumentZeichnung.builder().build());
        DokumentZeichnung zeichnung = DokumentZeichnung.builder()
            .status(DokumentZeichnungStatus.RELEASED)
            .bereich("SW")
            .unterzeichner("Hans Dampf")
            .anwendbar(false)
            .build();

        // act
        Optional<DokumentZeichnung> actual = repository.updateDokumentZeichnung("DUMMY", "master", zeichnung);

        // assert
        assertThat(actual).isNotEmpty();
        verify(mapperMock, times(1)).updateDokumentZeichnung(zeichnung, zeichnungEntity);
        verify(mapperMock, times(1)).toDomain(zeichnungEntity);
    }

    @Test
    void getDefaultZeichnungen_KeineParameter_ZeichnungenWerdenZurueckGegeben() {
        // arrange
        DokumentZeichnerEntity dokumentZeichner = DokumentZeichnerEntity.builder()
            .status(DokumentZeichnungStatus.AGREED)
            .bereich("SW")
            .unterzeichner("Hans Dampf")
            .build();
        given(dokumentZeichnerRepositoryMock.findAll()).willReturn(asList(dokumentZeichner));
        given(mapperMock.getDefaultZeichnungen(dokumentZeichner)).willReturn(DokumentZeichnung.builder().build());

        // act
        Collection<DokumentZeichnung> actual = repository.getDefaultZeichnungen();

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(1);

    }

    @Test
    void getSelektionsVektorProfile_KeineParameter_ProfileWerdenZurueckGegeben() {
        // arrange
        SelektionsVektorProfilEntity selektionsVektorProfil = SelektionsVektorProfilEntity.builder()
            .name("Test1")
            .build();
        given(selektionsVektorProfilRepositoryMock.findAll()).willReturn(asList(selektionsVektorProfil));

        given(mapperMock.toDomain(selektionsVektorProfil)).willReturn(SelektionsVektorProfil.builder().build());

        // act
        Collection<SelektionsVektorProfil> actual = repository.getSelektionsVektorProfile();

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(1);
    }

    @Test
    void getDokumentListe_ProjektPhaseNichtVorhanden_LeereListeWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(null);

        // act
        List<Dokument> actual = repository.getDokumentListe("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getDokumentListe_ProjektPhaseKeineDokumentVorhanden_LeereListeWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .dokumente(Collections.emptySet())
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);


        // act
        List<Dokument> actual = repository.getDokumentListe("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(0)).toDomain(any(DokumentEntity.class));
    }

    @Test
    void getDokumentListe_ProjektPhaseDokumenteVorhanden_ListeMit2DokumentenWirdZurueckGegeben() {
        // arrange
        DokumentEntity dokument1 = DokumentEntity.builder().name("Dok1").build();
        DokumentEntity dokument2 = DokumentEntity.builder().name("Dok2").build();
        TailoringEntity projektPhase = TailoringEntity.builder()
            .dokumente(Set.of(dokument1, dokument2))
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        given(mapperMock.toDomain(any(DokumentEntity.class))).willReturn(Dokument.builder().build());

        // act
        List<Dokument> actual = repository.getDokumentListe("DUMMY", "master");

        // assert
        assertThat(actual).hasSize(2);
        verify(mapperMock, times(2)).toDomain(any(DokumentEntity.class));
    }

    @Test
    void getDokument_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<Datei> actual = repository.getDokument("DUMMY", "master", "egal");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getDokument_ProjektPhaseVorhandenDokumentNicht_EmptyWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .dokumente(Collections.emptySet())
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        // act
        Optional<Datei> actual = repository.getDokument("DUMMY", "master", "egal");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getDokument_ProjektPhaseUndDokumentVorhande_ByteArrayWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .dokumente(Set.of(DokumentEntity.builder()
                .name("egal.pdf")
                .daten("Dummy Daten".getBytes(UTF_8))
                .build()))
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        // act
        Optional<Datei> actual = repository.getDokument("DUMMY", "master", "egal.pdf");

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getBytes()).isNotNull();
    }

    @Test
    void getScreeningSheetDatei_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetDatei("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getScreeningSheetDatei_PhaseMitScreeningSheetVorhandenDateiNicht_EmptyWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .data(null)
                .build())
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetDatei("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getScreeningSheetDatei_PhaseMitScreeningSheetMitDateiVorhanden_ByteArrayWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .data("ScreeningSheet".getBytes(UTF_8))
                .build())
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetDatei("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
    }

    @Test
    void getScreeningSheet_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getScreeningSheet_PhaseOhneScreeningSheetVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .build())
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);
        given(mapperMock.toScreeningSheetParameters(projektPhase.getScreeningSheet())).willReturn(null);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(1)).toScreeningSheetParameters(projektPhase.getScreeningSheet());
    }

    @Test
    void getScreeningSheet_ScreeningSheetVorhanden_ScreeningSheetWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .build())
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);
        given(mapperMock.toScreeningSheetParameters(projektPhase.getScreeningSheet())).willReturn(ScreeningSheet.builder().build());

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
        verify(mapperMock, times(1)).toScreeningSheetParameters(projektPhase.getScreeningSheet());
    }

    @Test
    void getProjektPhase_ProjektNichtUebergeben_EmptyWirdZurueckGegeben() {
        // arrange

        // act
        Optional<Tailoring> actual = repository.getTailoring(null, "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getProjektPhase_PhaseNichtUebergeben_EmptyWirdZurueckGegeben() {
        // arrange

        // act
        Optional<Tailoring> actual = repository.getTailoring("DUMMY", null);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getProjektPhase_ProjektPhaseVorhanden_PhaseWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .build())
            .build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        given(mapperMock.toDomain(projektPhase)).willReturn(Tailoring.builder().build());

        // act
        Optional<Tailoring> actual = repository.getTailoring("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
        verify(mapperMock, times(1)).toDomain(projektPhase);
    }

    @Test
    void deleteProjektPhase_ProjektPhaseNichtVorhanden_FalseWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("DUMMY", "master42")).willReturn(null);

        // act
        boolean actual = repository.deleteTailoring("DUMMY", "master42");

        // assert
        assertThat(actual).isFalse();
        verify(tailoringRepositoryMock, times(0)).delete(any());
    }

    @Test
    void deleteProjektPhase_ProjektPhaseVorhanden_TrueWirdZurueckGegeben() {
        // arrange
        TailoringEntity toDelete = TailoringEntity.builder().build();
        given(projektRepositoryMock.findTailoring("DUMMY", "master42")).willReturn(toDelete);

        // act
        boolean actual = repository.deleteTailoring("DUMMY", "master42");

        // assert
        assertThat(actual).isTrue();
        verify(tailoringRepositoryMock, times(1)).delete(toDelete);
    }

    @Test
    void deleteDocument_ProjektNichtVorhanden_FalseWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(null);

        // act
        boolean actual = repository.deleteDokument("SAMPLE", "master", "Demo");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void deleteDocument_DokumentNichtVorhanden_FalseWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(TailoringEntity.builder().dokumente(Collections.emptySet()).build());

        // act
        boolean actual = repository.deleteDokument("SAMPLE", "master", "NotExisting");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void deleteDocument_DokumentVorhanden_TrueWirdZurueckGegeben() {
        // arrange
        given(projektRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(TailoringEntity.builder()
                .dokumente(new HashSet(List.of(DokumentEntity.builder().name("DoBeDeleted").build())))
                .build());

        // act
        boolean actual = repository.deleteDokument("SAMPLE", "master", "DoBeDeleted");

        // assert
        assertThat(actual).isTrue();
    }

}
