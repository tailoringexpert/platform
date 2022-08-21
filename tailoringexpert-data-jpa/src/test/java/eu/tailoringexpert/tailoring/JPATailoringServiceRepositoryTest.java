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

import eu.tailoringexpert.domain.FileEntity;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.DocumentSignatureEntity;
import eu.tailoringexpert.domain.DocumentSigneeEntity;
import eu.tailoringexpert.domain.DocumentSignatureState;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.SelectionVectorProfileEntity;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.DokumentSigneeRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import eu.tailoringexpert.repository.TailoringRepository;
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
    ProjectRepository projectRepositoryMock;
    TailoringRepository tailoringRepositoryMock;
    SelectionVectorProfileRepository selectionVectorProfileRepositoryMock;
    DokumentSigneeRepository dokumentSigneeRepositoryMock;
    JPATailoringServiceRepository repository;

    @BeforeEach
    void setup() {
        this.mapperMock = mock(JPATailoringServiceRepositoryMapper.class);
        this.projectRepositoryMock = mock(ProjectRepository.class);
        this.tailoringRepositoryMock = mock(TailoringRepository.class);
        this.selectionVectorProfileRepositoryMock = mock(SelectionVectorProfileRepository.class);
        this.dokumentSigneeRepositoryMock = mock(DokumentSigneeRepository.class);
        this.repository = new JPATailoringServiceRepository(
            mapperMock,
            projectRepositoryMock,
            tailoringRepositoryMock,
                selectionVectorProfileRepositoryMock,
            dokumentSigneeRepositoryMock
        );
    }

    @Test
    void getProjekt_ProjektNichtVorhanden_EmptyErgebnis() {
        // arrange
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(null);

        // act
        Optional<Project> actual = repository.getProject("SAMPLE");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getProjekt_ProjektVorhanden_ProjektWirdZurueckGegeben() {
        // arrange
        ProjectEntity projekt = ProjectEntity.builder().build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        given(mapperMock.toDomain(projekt))
            .willReturn(Project.builder().build());

        // act
        Optional<Project> actual = repository.getProject("SAMPLE");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void updateKatalog_ProjektPhaseNichtVorhanden_EingabePhaseAlsErgebnis() {
        // arrange
        TailoringEntity projektPhaseToUpdate = TailoringEntity.builder()
            .name("master1")
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
            .tailorings(asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                projektPhaseToUpdate
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        Tailoring tailoring = Tailoring.builder()
            .name("master2")
            .build();

        // act
        Tailoring actual = repository.updateTailoring("SAMPLE", tailoring);

        // assert
        assertThat(actual).isNotNull();
        verify(mapperMock, times(0))
            .addCatalog(tailoring, projektPhaseToUpdate);
    }

    @Test
    void updateKatalog_ProjektPhaseVorhanden_ProjektPhaseAktualisiert() {
        // arrange
        TailoringEntity projektPhaseToUpdate = TailoringEntity.builder()
            .name("master1")
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
            .tailorings(asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                projektPhaseToUpdate
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
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
            .addCatalog(tailoring, projektPhaseToUpdate);
    }

    @Test
    void updateAnforderungDokument() {
        // arrange
        TailoringEntity projektPhaseToUpdate = TailoringEntity.builder()
            .name("master")
            .files(new HashSet<>())
            .build();
        given(projectRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(projektPhaseToUpdate);

        File document = File.builder().build();
        Tailoring tailoring = Tailoring.builder()
            .name("master")
            .files(asList(document))
            .build();

        given(mapperMock.toDomain(projektPhaseToUpdate))
            .willReturn(tailoring);

        // act
        Optional<Tailoring> actual = repository.updateFile("SAMPLE", "master", document);

        // assert
        assertThat(actual).isPresent();
        assertThat(projektPhaseToUpdate.getFiles()).isNotEmpty();
    }


    @Test
    void updateAnforderungDokument_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(null);

        File file = File.builder().build();

        // act
        Optional<Tailoring> actual = repository.updateFile("SAMPLE", "master", file);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateName_NullProjekt_NameWirdNichtAktualisiert() {
        // arrange
        given(projectRepositoryMock.findTailoring(null, "master"))
            .willReturn(null);

        // act
        Optional<Tailoring> actual = repository.updateName(null, "master1", "test");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateName_NullPhase_NameWirdNichtAktualisiert() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", null))
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
        given(projectRepositoryMock.findTailoring("DUMMY", "master"))
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
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<DocumentSignature> actual = repository.updateDocumentSignature("DUMMY", "master", DocumentSignature.builder().build());

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateDokumentZeichnung_ZeichnungBereichNichtVorhanden_EmptyWirdZurueckGegeben1() {
        // arrange
        DocumentSignatureEntity zeichnungEntity = DocumentSignatureEntity.builder()
            .state(DocumentSignatureState.AGREED)
            .faculty("SW")
            .signee("Hans Dampf")
            .applicable(true)
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(TailoringEntity.builder()
                .signatures(asList(
                    zeichnungEntity
                ))
                .build()
            );

        given(mapperMock.toDomain(zeichnungEntity)).willReturn(DocumentSignature.builder().build());
        DocumentSignature zeichnung = DocumentSignature.builder()
            .state(DocumentSignatureState.RELEASED)
            .faculty("Safety")
            .signee("Hans Dampf")
            .applicable(false)
            .build();

        // act
        Optional<DocumentSignature> actual = repository.updateDocumentSignature("DUMMY", "master", zeichnung);

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(0)).updateDocumentSignature(any(), any());
        verify(mapperMock, times(0)).toDomain(any(DocumentSignatureEntity.class));
    }

    @Test
    void updateDokumentZeichnung_ZeichnungVorhanden_NeueZeichnungWirdZurueckGegeben() {
        // arrange
        DocumentSignatureEntity zeichnungEntity = DocumentSignatureEntity.builder()
            .state(DocumentSignatureState.AGREED)
            .faculty("SW")
            .signee("Hans Dampf")
            .applicable(true)
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(TailoringEntity.builder()
                .signatures(asList(
                    zeichnungEntity
                ))
                .build()
            );

        given(mapperMock.toDomain(zeichnungEntity)).willReturn(DocumentSignature.builder().build());
        DocumentSignature zeichnung = DocumentSignature.builder()
            .state(DocumentSignatureState.RELEASED)
            .faculty("SW")
            .signee("Hans Dampf")
            .applicable(false)
            .build();

        // act
        Optional<DocumentSignature> actual = repository.updateDocumentSignature("DUMMY", "master", zeichnung);

        // assert
        assertThat(actual).isNotEmpty();
        verify(mapperMock, times(1)).updateDocumentSignature(zeichnung, zeichnungEntity);
        verify(mapperMock, times(1)).toDomain(zeichnungEntity);
    }

    @Test
    void getDefaultZeichnungen_KeineParameter_ZeichnungenWerdenZurueckGegeben() {
        // arrange
        DocumentSigneeEntity dokumentZeichner = DocumentSigneeEntity.builder()
            .state(DocumentSignatureState.AGREED)
            .faculty("SW")
            .signee("Hans Dampf")
            .build();
        given(dokumentSigneeRepositoryMock.findAll()).willReturn(asList(dokumentZeichner));
        given(mapperMock.getDefaultSignatures(dokumentZeichner)).willReturn(DocumentSignature.builder().build());

        // act
        Collection<DocumentSignature> actual = repository.getDefaultSignatures();

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(1);

    }

    @Test
    void getSelektionsVektorProfile_KeineParameter_ProfileWerdenZurueckGegeben() {
        // arrange
        SelectionVectorProfileEntity selektionsVektorProfil = SelectionVectorProfileEntity.builder()
            .name("Test1")
            .build();
        given(selectionVectorProfileRepositoryMock.findAll()).willReturn(asList(selektionsVektorProfil));

        given(mapperMock.toDomain(selektionsVektorProfil)).willReturn(SelectionVectorProfile.builder().build());

        // act
        Collection<SelectionVectorProfile> actual = repository.getSelectionVectorProfile();

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(1);
    }

    @Test
    void getDokumentListe_ProjektPhaseNichtVorhanden_LeereListeWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(null);

        // act
        List<File> actual = repository.getFileList("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getDokumentListe_ProjektPhaseKeineDokumentVorhanden_LeereListeWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .files(Collections.emptySet())
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);


        // act
        List<File> actual = repository.getFileList("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(0)).toDomain(any(FileEntity.class));
    }

    @Test
    void getDokumentListe_ProjektPhaseDokumenteVorhanden_ListeMit2DokumentenWirdZurueckGegeben() {
        // arrange
        FileEntity dokument1 = FileEntity.builder().name("Dok1").build();
        FileEntity dokument2 = FileEntity.builder().name("Dok2").build();
        TailoringEntity projektPhase = TailoringEntity.builder()
            .files(Set.of(dokument1, dokument2))
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        given(mapperMock.toDomain(any(FileEntity.class))).willReturn(File.builder().build());

        // act
        List<File> actual = repository.getFileList("DUMMY", "master");

        // assert
        assertThat(actual).hasSize(2);
        verify(mapperMock, times(2)).toDomain(any(FileEntity.class));
    }

    @Test
    void getDokument_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<File> actual = repository.getFile("DUMMY", "master", "egal");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getDokument_ProjektPhaseVorhandenDokumentNicht_EmptyWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .files(Collections.emptySet())
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        // act
        Optional<File> actual = repository.getFile("DUMMY", "master", "egal");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getDokument_ProjektPhaseUndDokumentVorhande_ByteArrayWirdZurueckGegeben() {
        // arrange
        TailoringEntity projektPhase = TailoringEntity.builder()
            .files(Set.of(FileEntity.builder()
                .name("egal.pdf")
                .data("Dummy Daten".getBytes(UTF_8))
                .build()))
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        // act
        Optional<File> actual = repository.getFile("DUMMY", "master", "egal.pdf");

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getData()).isNotNull();
    }

    @Test
    void getScreeningSheetDatei_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile("DUMMY", "master");

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
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile("DUMMY", "master");

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
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
    }

    @Test
    void getScreeningSheet_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

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
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);
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
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);
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
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

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
        given(projectRepositoryMock.findTailoring("DUMMY", "master42")).willReturn(null);

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
        given(projectRepositoryMock.findTailoring("DUMMY", "master42")).willReturn(toDelete);

        // act
        boolean actual = repository.deleteTailoring("DUMMY", "master42");

        // assert
        assertThat(actual).isTrue();
        verify(tailoringRepositoryMock, times(1)).delete(toDelete);
    }

    @Test
    void deleteDocument_ProjektNichtVorhanden_FalseWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(null);

        // act
        boolean actual = repository.deleteFile("SAMPLE", "master", "Demo");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void deleteDocument_DokumentNichtVorhanden_FalseWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(TailoringEntity.builder().files(Collections.emptySet()).build());

        // act
        boolean actual = repository.deleteFile("SAMPLE", "master", "NotExisting");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void deleteDocument_DokumentVorhanden_TrueWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(TailoringEntity.builder()
                .files(new HashSet(List.of(FileEntity.builder().name("DoBeDeleted").build())))
                .build());

        // act
        boolean actual = repository.deleteFile("SAMPLE", "master", "DoBeDeleted");

        // assert
        assertThat(actual).isTrue();
    }

}
