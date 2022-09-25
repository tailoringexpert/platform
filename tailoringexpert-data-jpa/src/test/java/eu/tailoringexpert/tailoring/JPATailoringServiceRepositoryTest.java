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
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.NoteEntity;
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
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.List.copyOf;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    void getProject_ProjectNotExists_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(null);

        // act
        Optional<Project> actual = repository.getProject("SAMPLE");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getProject_ProjectExists_ProjectReturned() {
        // arrange
        ProjectEntity project = ProjectEntity.builder().build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        given(mapperMock.toDomain(project)).willReturn(Project.builder().build());

        // act
        Optional<Project> actual = repository.getProject("SAMPLE");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void updateTailoring_TaioloringNotExists_TailoringNotUpdated() {
        // arrange
        TailoringEntity tailoringToUpdate = TailoringEntity.builder()
            .name("master1")
            .build();
        ProjectEntity project = ProjectEntity.builder()
            .tailorings(asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                tailoringToUpdate
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        Tailoring tailoring = Tailoring.builder()
            .name("master2")
            .build();

        // act
        Tailoring actual = repository.updateTailoring("SAMPLE", tailoring);

        // assert
        assertThat(actual).isNotNull();
        verify(mapperMock, times(0)).updateTailoring(tailoring, tailoringToUpdate);
    }

    @Test
    void updateTailoring_TailoringExists_TailoringUpdated() {
        // arrange
        TailoringEntity tailoringToUpdate = TailoringEntity.builder()
            .name("master1")
            .build();
        ProjectEntity project = ProjectEntity.builder()
            .tailorings(asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                tailoringToUpdate
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        Tailoring tailoring = Tailoring.builder()
            .name("master1")
            .build();

        given(mapperMock.toDomain(tailoringToUpdate))
            .willReturn(tailoring);

        // act
        Tailoring actual = repository.updateTailoring("SAMPLE", tailoring);

        // assert
        assertThat(actual).isNotNull();
        verify(mapperMock, times(1)).updateTailoring(tailoring, tailoringToUpdate);
    }

    @Test
    void updateFile_FileNotNull_FileAddedToTailoring() {
        // arrange
        TailoringEntity tailoringToUpdate = TailoringEntity.builder()
            .name("master")
            .files(new HashSet<>())
            .build();
        given(projectRepositoryMock.findTailoring("SAMPLE", "master")).willReturn(tailoringToUpdate);

        File file = File.builder().build();
        Tailoring tailoring = Tailoring.builder()
            .name("master")
            .files(asList(file))
            .build();
        given(mapperMock.toDomain(tailoringToUpdate)).willReturn(tailoring);

        // act
        Optional<Tailoring> actual = repository.updateFile("SAMPLE", "master", file);

        // assert
        assertThat(actual).isPresent();
        assertThat(tailoringToUpdate.getFiles()).isNotEmpty();
    }


    @Test
    void updateFile_TailoringNull_FileNotAddedEmptyReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("SAMPLE", "master")).willReturn(null);

        File file = File.builder().build();

        // act
        Optional<Tailoring> actual = repository.updateFile("SAMPLE", "master", file);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateName_ProjectNull_NameNotUpdated() {
        // arrange
        given(projectRepositoryMock.findTailoring(null, "master"))
            .willReturn(null);

        // act
        Optional<Tailoring> actual = repository.updateName(null, "master1", "test");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateName_TailoringNull_NameNotUpdated() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", null)).willReturn(null);

        // act
        Optional<Tailoring> actual = repository.updateName("DUMMY", null, "test");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateName_NewNameNotUsedBefore_NameUpdated() {
        // arrange
        TailoringEntity tailoring = new TailoringEntity();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(tailoring);

        given(mapperMock.toDomain(tailoring))
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
    void updateDocumentSignature_TailoringNotExists_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<DocumentSignature> actual = repository.updateDocumentSignature("DUMMY", "master", DocumentSignature.builder().build());

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateDocumentSignature_FacultyNotExists_EmptyReturned() {
        // arrange
        DocumentSignatureEntity signatureEntity = DocumentSignatureEntity.builder()
            .state(DocumentSignatureState.AGREED)
            .faculty("SW")
            .signee("Hans Dampf")
            .applicable(true)
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(TailoringEntity.builder()
                .signatures(asList(
                    signatureEntity
                ))
                .build()
            );

        given(mapperMock.toDomain(signatureEntity)).willReturn(DocumentSignature.builder().build());
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
    void updateDocumentSignature_SignaturesExists_NeueZeichnungWirdZurueckGegeben() {
        // arrange
        DocumentSignatureEntity signatureEntity = DocumentSignatureEntity.builder()
            .state(DocumentSignatureState.AGREED)
            .faculty("SW")
            .signee("Hans Dampf")
            .applicable(true)
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master"))
            .willReturn(TailoringEntity.builder()
                .signatures(asList(
                    signatureEntity
                ))
                .build()
            );

        given(mapperMock.toDomain(signatureEntity)).willReturn(DocumentSignature.builder().build());
        DocumentSignature signature = DocumentSignature.builder()
            .state(DocumentSignatureState.RELEASED)
            .faculty("SW")
            .signee("Hans Dampf")
            .applicable(false)
            .build();

        // act
        Optional<DocumentSignature> actual = repository.updateDocumentSignature("DUMMY", "master", signature);

        // assert
        assertThat(actual).isNotEmpty();
        verify(mapperMock, times(1)).updateDocumentSignature(signature, signatureEntity);
        verify(mapperMock, times(1)).toDomain(signatureEntity);
    }

    @Test
    void getDefaultSignatures_NoInputNeede_SignaturesReturned() {
        // arrange
        DocumentSigneeEntity signee = DocumentSigneeEntity.builder()
            .state(DocumentSignatureState.AGREED)
            .faculty("SW")
            .signee("Hans Dampf")
            .build();
        given(dokumentSigneeRepositoryMock.findAll()).willReturn(asList(signee));
        given(mapperMock.getDefaultSignatures(signee)).willReturn(DocumentSignature.builder().build());

        // act
        Collection<DocumentSignature> actual = repository.getDefaultSignatures();

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(1);

    }

    @Test
    void getSelectionVectorProfile_NoParameterNeeded_ProfilesReturned() {
        // arrange
        SelectionVectorProfileEntity selectionVectorProfile = SelectionVectorProfileEntity.builder()
            .name("Test1")
            .build();
        given(selectionVectorProfileRepositoryMock.findAll()).willReturn(asList(selectionVectorProfile));

        given(mapperMock.toDomain(selectionVectorProfile)).willReturn(SelectionVectorProfile.builder().build());

        // act
        Collection<SelectionVectorProfile> actual = repository.getSelectionVectorProfile();

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(1);
    }

    @Test
    void getFileList_TailoringNotExists_EmptyListReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<List<File>> actual = repository.getFileList("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getFileList_TailoringExistsNoFiles_OptionalEmptyListReturned() {
        // arrange
        TailoringEntity tailoring = TailoringEntity.builder()
            .files(Collections.emptySet())
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(tailoring);

        // act
        Optional<List<File>> actual = repository.getFileList("DUMMY", "master");

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get()).isEmpty();
        verify(mapperMock, times(0)).toDomain(any(FileEntity.class));
    }

    @Test
    void getFileList_TailoringWithFilesExists_ListWithFilesReturned() {
        // arrange
        FileEntity file1 = FileEntity.builder().name("Dok1").build();
        FileEntity file2 = FileEntity.builder().name("Dok2").build();
        TailoringEntity projektPhase = TailoringEntity.builder()
            .files(Set.of(file1, file2))
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(projektPhase);

        given(mapperMock.toDomain(any(FileEntity.class))).willReturn(File.builder().build());

        // act
        Optional<List<File>> actual = repository.getFileList("DUMMY", "master");

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get()).hasSize(2);
        verify(mapperMock, times(2)).toDomain(any(FileEntity.class));
    }

    @Test
    void getFile_TailoringNotExists_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<File> actual = repository.getFile("DUMMY", "master", "egal");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getFile_FileNotExists_EmptyReturned() {
        // arrange
        TailoringEntity tailoring = TailoringEntity.builder()
            .files(Collections.emptySet())
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(tailoring);

        // act
        Optional<File> actual = repository.getFile("DUMMY", "master", "egal");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getFile_FileExists_ByteArrayReturned() {
        // arrange
        TailoringEntity tailoring = TailoringEntity.builder()
            .files(Set.of(FileEntity.builder()
                .name("egal.pdf")
                .data("Dummy Daten".getBytes(UTF_8))
                .build()))
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(tailoring);

        // act
        Optional<File> actual = repository.getFile("DUMMY", "master", "egal.pdf");

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getData()).isNotNull();
    }

    @Test
    void getScreeningSheetFile_TailoringNotExists_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getScreeningSheetFile_ScreeningSheetWithoutFile_EmptyReturned() {
        // arrange
        TailoringEntity tailoring = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .data(null)
                .build())
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(tailoring);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getScreeningSheetFile_ScreeningSheetFileExists_ByteArrayReturned() {
        // arrange
        TailoringEntity tailoring = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .data("ScreeningSheet".getBytes(UTF_8))
                .build())
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(tailoring);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
    }

    @Test
    void getScreeningSheet_TailoringNotExists_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(null);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getScreeningSheet_ScreeningSheetVorhandenNull_EmptyReturned() {
        // arrange
        TailoringEntity tailoring = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .build())
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(tailoring);
        given(mapperMock.toScreeningSheetParameters(tailoring.getScreeningSheet())).willReturn(null);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(1)).toScreeningSheetParameters(tailoring.getScreeningSheet());
    }

    @Test
    void getScreeningSheet_ScreeningSheetExists_ScreeningSheetReturnd() {
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
    void getTailoring_ProjectNull_EmptyReturned() {
        // arrange

        // act
        Optional<Tailoring> actual = repository.getTailoring(null, "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getTailoring_TailoringNull_EmptyReturned() {
        // arrange

        // act
        Optional<Tailoring> actual = repository.getTailoring("DUMMY", null);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getTailoring_TailoringExists_TailoringReturned() {
        // arrange
        TailoringEntity tailoring = TailoringEntity.builder()
            .screeningSheet(ScreeningSheetEntity.builder()
                .build())
            .build();
        given(projectRepositoryMock.findTailoring("DUMMY", "master")).willReturn(tailoring);

        given(mapperMock.toDomain(tailoring)).willReturn(Tailoring.builder().build());

        // act
        Optional<Tailoring> actual = repository.getTailoring("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
        verify(mapperMock, times(1)).toDomain(tailoring);
    }

    @Test
    void deleteTailoring_TailoringNotExists_FalseReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("DUMMY", "master42")).willReturn(null);

        // act
        boolean actual = repository.deleteTailoring("DUMMY", "master42");

        // assert
        assertThat(actual).isFalse();
        verify(tailoringRepositoryMock, times(0)).delete(any());
    }

    @Test
    void deleteTailoring_TailoringExists_TrueReturned() {
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
    void deleteFile_ProjectNotExists_FalseReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("SAMPLE", "master")).willReturn(null);

        // act
        boolean actual = repository.deleteFile("SAMPLE", "master", "Demo");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void deleteFile_FileNotExists_FalseReturned() {
        // arrange
        given(projectRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(TailoringEntity.builder().files(Collections.emptySet()).build());

        // act
        boolean actual = repository.deleteFile("SAMPLE", "master", "NotExisting");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void deleteFile_FileExists_TrueReturned() {
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

    @Test
    void addNote_TailoringNotExists_EmptyReturned() {
        // arrange
        Note note = Note.builder().build();
        given(projectRepositoryMock.findTailoring("SAMPLE", "master"))
            .willReturn(null);

        // act
        Optional<Tailoring> actual = repository.addNote("SAMPLE", "master", note);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void addNote_TailoringExists_NoteAddedTailoringReturned() {
        // arrange
        List<NoteEntity> notes = new ArrayList<>();
        notes.add(NoteEntity.builder().number(1).text("Note1").build());
        Note note = Note.builder().number(2).text("Note 2").build();

        TailoringEntity tailoring = TailoringEntity.builder().notes(notes).build();
        given(projectRepositoryMock.findTailoring("SAMPLE", "master")).willReturn(tailoring);

        given(mapperMock.toEntity(note)).willAnswer(invocation -> {
            Note toAdd = invocation.getArgument(0);
            return NoteEntity.builder().number(toAdd.getNumber()).text(toAdd.getText()).build();
        });

        given(mapperMock.toDomain(tailoring)).willReturn(Tailoring.builder().build());

        // act
        Optional<Tailoring> actual = repository.addNote("SAMPLE", "master", note);

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(tailoring.getNotes()).hasSize(2);
        assertThat(copyOf(tailoring.getNotes()).get(1).getNumber()).isEqualTo(2);
        assertThat(copyOf(tailoring.getNotes()).get(1).getText()).isEqualTo("Note 2");
    }

}
