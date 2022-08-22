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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.requirement.RequirementService;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.DocumentSignatureState;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringInformation;
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

import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.TailoringState.ACTIVE;

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
    private DocumentService documentServiceMock;
    private RequirementService requirementServiceMock;
    private Function<byte[], Map<String, Collection<ImportRequirement>>> tailoringAnforderungFileReaderMock;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(TailoringServiceRepository.class);
        this.mapperMock = mock(TailoringServiceMapper.class);
        this.documentServiceMock = mock(DocumentService.class);
        this.requirementServiceMock = mock(RequirementService.class);
        this.tailoringAnforderungFileReaderMock = mock(Function.class);
        this.service = new TailoringServiceImpl(
            repositoryMock,
            mapperMock,
            documentServiceMock,
            requirementServiceMock,
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
        given(repositoryMock.getProject("DUMMY")).willReturn(empty());

        // act
        Optional<Tailoring> actual = service.addFile("DUMMY", "master", "dummy.pdf", data);

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

        given(repositoryMock.getProject("DUMMY")).willReturn(of(
            Project.builder()
                .tailorings(Collections.emptyList())
                .build())
        );

        // act
        Optional<Tailoring> actual = service.addFile("DUMMY", "master", "dummy.pdf", data);

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

        given(repositoryMock.updateFile(eq("SAMPLE"), eq("master"), any()))
            .willAnswer(invocation -> of(Tailoring.builder()
                .files(asList(
                    File.builder()
                        .name("dummy.pdf")
                        .build()))
                .build())
            );


        // act
        Optional<Tailoring> actual = service.addFile("SAMPLE", "master", "dummy.pdf", data);

        // assert
        assertThat(actual).isPresent();

        assertThat(actual.get().getFiles()).hasSize(1);
        assertThat(actual.get().getFiles().iterator().next().getName()).isEqualTo("dummy.pdf");
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
        Optional<Tailoring> actual = service.addFile("SAMPLE", "master", "dummy.pdf", data);

        // assert
        verify(repositoryMock, times(0)).updateFile(anyString(), anyString(), any());
        assertThat(actual).isEmpty();
    }

    @Test
    void getKatalog_ProjektNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getCatalog(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKatalog_PhasetNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getCatalog("DUMMY", null));

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
                    .catalog(Catalog.<TailoringRequirement>builder().build())
                    .build())
            );

        // act
        Optional<Catalog<TailoringRequirement>> actual = service.getCatalog("SAMPLE", "master");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void getKatalog_ProjektNichtVorhanden_KatalogEmpty() {
        // arrange
        given(repositoryMock.getProject("SAMPLE"))
            .willAnswer(invocation -> empty());

        // act
        Optional<Catalog<TailoringRequirement>> actual = service.getCatalog("SAMPLE", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getKatalog_PhaseNichtVorhanden_KatalogEmpty() {
        // arrange
        given(repositoryMock.getProject("SAMPLE"))
            .willAnswer(invocation -> of(
                Project.builder()
                    .identifier(invocation.getArgument(0))
                    .tailorings(asList(
                        Tailoring.builder()
                            .name("master1")
                            .catalog(Catalog.<TailoringRequirement>builder().build())
                            .build()
                    ))
                    .build())
            );

        // act
        Optional<Catalog<TailoringRequirement>> actual = service.getCatalog("SAMPLE", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void createAnforderungDokument_PhaseNichtVorhanden_KeinDokumentErstellt() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "master25")).willReturn(empty());

        // act
        Optional<File> actual = service.createRequirementDocument("DUMMY", "master25");

        // assert
        assertThat(actual).isEmpty();
        verify(documentServiceMock, times(0)).createRequirementDocument(any(), any());
    }

    @Test
    void createAnforderungDokument() {
        // arrange
        Collection<DocumentSignature> zeichnungen = asList(
            DocumentSignature.builder()
                .state(DocumentSignatureState.PREPARED)
                .faculty("Safety")
                .signee("B. Safe")
                .build(),
            DocumentSignature.builder()
                .state(DocumentSignatureState.AGREED)
                .faculty("Software")
                .signee("Software Tuppes")
                .build(),
            DocumentSignature.builder()
                .state(DocumentSignatureState.AGREED)
                .faculty("Project Management")
                .signee("P. Management/RD-???")
                .build(),
            DocumentSignature.builder()
                .state(DocumentSignatureState.RELEASED)
                .faculty("Head of Product Assurance")
                .signee("Head Hunter/RD-PS")
                .build()
        );

        Catalog<TailoringRequirement> catalog = Catalog.<TailoringRequirement>builder()
            .version("8.2.1")
            .build();

        Tailoring tailoring = Tailoring.builder()
            .name("master1")
            .catalog(catalog)
            .screeningSheet(ScreeningSheet.builder()
                .parameters(asList(ScreeningSheetParameter.builder().category(ScreeningSheetDataProviderSupplier.Kuerzel.getName()).value("SAMPLE").build()))
                .build())
            .signatures(zeichnungen)
            .build();
        given(repositoryMock.getTailoring("SAMPLE", "master1"))
            .willAnswer(invocation -> of(tailoring));

        given(documentServiceMock.createRequirementDocument(eq(tailoring), any()))
            .willReturn(of(File.builder().build()));

        // act
        Optional<File> actual = service.createRequirementDocument("SAMPLE", "master1");

        // assert
        assertThat(actual).isPresent();
        verify(documentServiceMock, times(1))
            .createRequirementDocument(eq(tailoring), any());
    }

    @Test
    void getAnforderungen_NullProjekt_ExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getRequirements(null, "master", "1.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderungen_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getRequirements("Dummy", null, "1.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderungen_NullKapitel_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getRequirements("Dummy", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderungen_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TailoringService serviceSpy = spy(service);
        doReturn(empty()).when(serviceSpy).getChapter(anyString(), anyString(), any());

        // act
        Optional<List<TailoringRequirement>> actual = serviceSpy.getRequirements("Dummy", "master1", "1.1");

        // assert
        assertThat(actual).isEmpty();
        verify(serviceSpy, times(1)).getChapter("Dummy", "master1", "1.1");
    }

    @Test
    void getAnforderungen_KapitelNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TailoringService serviceSpy = spy(service);
        doReturn(empty()).when(serviceSpy).getChapter(anyString(), anyString(), any());

        // act
        Optional<List<TailoringRequirement>> actual = serviceSpy.getRequirements("Dummy", "master", "1.1");

        // assert
        assertThat(actual).isEmpty();
        verify(serviceSpy, times(1)).getChapter("Dummy", "master", "1.1");
    }

    @Test
    void getAnforderungen_KapitelVorhanden_AnforderungenWerdenZurueckGegeben() {
        // arrange
        TailoringService serviceSpy = spy(service);
        doReturn(of(
            Chapter.<TailoringRequirement>builder()
                .number("1.1")
                .requirements(asList(
                    TailoringRequirement.builder()
                        .text("Requirement 1")
                        .build(),
                    TailoringRequirement.builder()
                        .text("Requirement 2")
                        .build()
                ))
                .build()))
            .when(serviceSpy).getChapter("Dummy", "master", "1.1");

        // act
        Optional<List<TailoringRequirement>> actual = serviceSpy.getRequirements("Dummy", "master", "1.1");

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get()).hasSize(2);
        verify(serviceSpy, times(1)).getChapter("Dummy", "master", "1.1");
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
        Throwable actual = catchThrowable(() -> service.getSelectionVector(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getSelektionsVektor_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getSelectionVector("Dummy", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getSelektionsVektor_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(empty());

        // act
        Optional<SelectionVector> actual = service.getSelectionVector("Dummy", "master1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master1");
    }

    @Test
    void getSelektionsVektor_PhaseMitSelektionsVektorVorhanden_SelektionsVektorWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(of(
            Tailoring.builder().selectionVector(SelectionVector.builder().build()).build()
        ));

        // act
        Optional<SelectionVector> actual = service.getSelectionVector("Dummy", "master");

        // assert
        assertThat(actual).isNotEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master");
    }


    @Test
    void getKapitel_NullProjekt_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getChapter(null, "master", "1.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getChapter("Dummy", null, "1.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_NullKapitel_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getChapter("Dummy", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_ProjektPhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "master")).willReturn(empty());

        // act
        Optional<Chapter<TailoringRequirement>> actual = service.getChapter("DUMMY", "master", "1.1");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getKapitel_KapitelVorhanden_KapitelZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring("Dummy", "master")).willReturn(of(
            Tailoring.builder()
                .catalog(Catalog.<TailoringRequirement>builder()
                    .toc(Chapter.<TailoringRequirement>builder()
                        .chapters(asList(
                            Chapter.<TailoringRequirement>builder()
                                .number("1")
                                .chapters(asList(
                                    Chapter.<TailoringRequirement>builder()
                                        .number("1.1")
                                        .build()
                                ))
                                .build()
                        ))
                        .build())
                    .build())
                .build()));

        // act
        Optional<Chapter<TailoringRequirement>> actual = service.getChapter("Dummy", "master", "1.1");

        // assert
        assertThat(actual).isPresent();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master");
    }

    @Test
    void getDokumentZeichnungen_NullProjekt_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getDocumentSignatures(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDokumentZeichnungen_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.getDocumentSignatures("Dummy", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getDokumentZeichnungen_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(empty());

        // act
        Optional<Collection<DocumentSignature>> actual = service.getDocumentSignatures("Dummy", "master1");

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master1");
    }

    @Test
    void getDokumentZeichnungen_PhaseMitDokumentZeichnungVorhanden_DokumentZeichnungWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring(any(), any())).willReturn(of(
            Tailoring.builder().signatures(asList(
                    DocumentSignature.builder()
                        .faculty("Software")
                        .signee("Hans Dampf")
                        .state(DocumentSignatureState.AGREED)
                        .build()
                ))
                .build()
        ));

        // act
        Optional<Collection<DocumentSignature>> actual = service.getDocumentSignatures("Dummy", "master");

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).hasSize(1);
        verify(repositoryMock, times(1)).getTailoring("Dummy", "master");
    }

    @Test
    void updateDokumentZeichnung_NullProjekt_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.updateDocumentSignature(null, "master", DocumentSignature.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateDokumentZeichnung_NullPhase_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.updateDocumentSignature("Dummy", null, DocumentSignature.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateDokumentZeichnung_NullZeichnung_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.updateDocumentSignature("Dummy", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateDokumentZeichnung_ZeichnungNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        DocumentSignature zeichnung = DocumentSignature.builder()
            .faculty("Software")
            .signee("Hans Dampf")
            .state(DocumentSignatureState.AGREED)
            .build();

        given(repositoryMock.updateDocumentSignature(anyString(), anyString(), any(DocumentSignature.class))).willReturn(empty());
        // act
        Optional<DocumentSignature> actual = service.updateDocumentSignature("Dummy", "master", zeichnung);

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).updateDocumentSignature("Dummy", "master", zeichnung);
    }

    @Test
    void updateDokumentZeichnung_ZeichnungVorhanden_AktualisierteZeichnungWirdZurueckGegeben() {
        // arrange
        DocumentSignature zeichnung = DocumentSignature.builder()
            .faculty("Software")
            .signee("Hans Dampf")
            .state(DocumentSignatureState.AGREED)
            .build();

        given(repositoryMock.updateDocumentSignature("Dummy", "master", zeichnung)).willReturn(of(zeichnung));
        // act
        Optional<DocumentSignature> actual = service.updateDocumentSignature("Dummy", "master", zeichnung);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .contains(zeichnung);
        verify(repositoryMock, times(1)).updateDocumentSignature("Dummy", "master", zeichnung);
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
            .parameters(asList(ScreeningSheetParameter.builder().category(ScreeningSheetDataProviderSupplier.Phase.getName()).value(asList(E, F)).build()))
            .selectionVector(SelectionVector.builder().build())
            .build();

        SelectionVector anzuwendenderSelectionVector = SelectionVector.builder().build();

        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder()
                .chapters(asList(
                    Chapter.<BaseRequirement>builder()
                        .number("1")
                        .chapters(asList(
                            Chapter.<BaseRequirement>builder()
                                .number("1.1")
                                .build()
                        ))
                        .build()
                ))
                .build())
            .build();

        List<DocumentSignature> defaultZeichnungen = Collections.emptyList();
        given(repositoryMock.getDefaultSignatures()).willReturn(defaultZeichnungen);

        given(mapperMock.toTailoringCatalog(catalog, screeningSheet, anzuwendenderSelectionVector)).willReturn(Catalog.<TailoringRequirement>builder().build());

        // act
        Tailoring actual = service.createTailoring("master1", "1000", screeningSheet, anzuwendenderSelectionVector, catalog);

        // assert
        assertThat(actual.getName()).isEqualTo("master1");
        assertThat(actual.getScreeningSheet()).isEqualTo(screeningSheet);
        assertThat(actual.getSelectionVector()).isEqualTo(anzuwendenderSelectionVector);
        assertThat(actual.getCatalog()).isNotNull();
        assertThat(actual.getSignatures()).isEqualTo(defaultZeichnungen);
        assertThat(actual.getState()).isEqualTo(ACTIVE);
        assertThat(actual.getPhases()).containsOnly(E, F);

        verify(mapperMock, times(1)).toTailoringCatalog(catalog, screeningSheet, anzuwendenderSelectionVector);
        verify(repositoryMock, times(1)).getDefaultSignatures();
    }


    @Test
    void createDokumente_ProjektNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.createDocuments(null, "master"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDokumente_PhaseNull_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.createDocuments("DUMMY", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDokumente_PhaseNichtVorhanden_EmptyWirdZurueckgegeben() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "master1")).willReturn(empty());

        // act
        Optional<File> actual = service.createDocuments("DUMMY", "master1");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void createDokumente_TailoringExist_ZipReturned() throws IOException {
        // arrange
        Tailoring tailoring = Tailoring.builder().name("master").build();
        given(repositoryMock.getTailoring("DUMMY", "master")).willReturn(of(tailoring));

        List<File> dokumente = asList(
            File.builder()
                .name("DUMMY-KATALOG.pdf")
                .data("Testdokument".getBytes(UTF_8))
                .build()
        );
        given(documentServiceMock.createAll(eq(tailoring), any())).willReturn(dokumente);

        // act
        Optional<File> actual = service.createDocuments("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getName()).isEqualTo("DUMMY-master.zip");
        assertThat(actual.get().getType()).isEqualTo("zip");

        Collection<String> zipDateien = dateiNamenImZip(actual.get().getData());
        assertThat(zipDateien)
            .hasSize(1)
            .containsExactly("DUMMY-KATALOG.pdf");
    }

    @Test
    void createVergleichsDokument_PhaseNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(repositoryMock.getTailoring("DUMMY", "master")).willReturn(empty());

        // act
        Optional<File> actual = service.createComparisonDocument("DUMMY", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void createVergleichsDokument_PhaseVorhanden_VergeleichsdokuemnWirdZurueckGegeben() {
        // arrange
        Tailoring tailoring = Tailoring.builder().build();
        given(repositoryMock.getTailoring("DUMMY", "master")).willReturn(of(tailoring));

        given(documentServiceMock.createComparisonDocument(eq(tailoring), any())).willReturn(of(File.builder().build()));

        // act
        Optional<File> actual = service.createComparisonDocument("DUMMY", "master");

        // assert
        assertThat(actual).isNotEmpty();
        verify(documentServiceMock, times(1)).createComparisonDocument(eq(tailoring), any());
    }


    @Test
    void updateAusgewaehlteAnforderungen_ProjektNull_NullPointerExceptionWirdGeworfen() {
        // arrange
        String project = null;
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer File".getBytes(UTF_8);

        // act
        Throwable actual = catchThrowable(() -> service.updateSelectedRequirements(project, phase, data));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAusgewaehlteAnforderungen_PhaseNull_NullPointerExceptionWirdGeworfen() throws IOException {
        // arrange
        String project = "DUMMY";
        String phase = null;
        byte[] data = "Filereader wird gemockt. Kein parsen einer File".getBytes(UTF_8);

        // act
        Throwable actual = catchThrowable(() -> service.updateSelectedRequirements(project, phase, data));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAusgewaehlteAnforderungen_DataNull_DateiWirdNichtEingelesenVoidReturn() {
        // arrange
        String project = "DUMMY";
        String phase = "master";

        // act
        service.updateSelectedRequirements(project, phase, null);

        // assert
        verify(tailoringAnforderungFileReaderMock, times(0)).apply(any());
    }

    @Test
    void updateAusgewaehlteAnforderungen_AnforderungMitUngueltigemStatus_AnforderungMitUngueltigemStatusNichtVerarbeitet() {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer File".getBytes(UTF_8);

        given(tailoringAnforderungFileReaderMock.apply(data)).willReturn(Map.ofEntries(
                new AbstractMap.SimpleEntry<>("1", asList(
                    ImportRequirement.builder().position("a").applicable("JEIN").build(),
                    ImportRequirement.builder().position("b").applicable("NEIN").build()
                ))
            )
        );

        // act
        service.updateSelectedRequirements(project, phase, data);

        // assert
        verify(requirementServiceMock, times(0)).handleSelected(eq("DUMMY"), eq("master"), eq("1"), eq("a"), any());
        verify(requirementServiceMock, times(1)).handleSelected("DUMMY", "master", "1", "b", false);
    }

    @Test
    void updateAusgewaehlteAnforderungen_AnforderungenMitGueltigemStatus_AnforderungenWerdenVerarbeitet() {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer File".getBytes(UTF_8);

        given(tailoringAnforderungFileReaderMock.apply(data)).willReturn(Map.ofEntries(
                new AbstractMap.SimpleEntry<>("1", asList(
                    ImportRequirement.builder().position("a").applicable("JA").build(),
                    ImportRequirement.builder().position("b").applicable("NEIN").build()
                ))
            )
        );

        // act
        service.updateSelectedRequirements(project, phase, data);

        // assert
        verify(requirementServiceMock, times(1)).handleSelected("DUMMY", "master", "1", "a", true);
        verify(requirementServiceMock, times(1)).handleSelected("DUMMY", "master", "1", "b", false);
    }


    @Test
    void updateAusgewaehlteAnforderungen_AnforderungenMitGueltigemStatusUndTextanpassung_AnforderungenWerdenVerarbeitet() {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer File".getBytes(UTF_8);

        given(tailoringAnforderungFileReaderMock.apply(data)).willReturn(Map.ofEntries(
                new AbstractMap.SimpleEntry<>("1", asList(
                    ImportRequirement.builder().position("a").applicable("JA").text("Dies ist der neue Text").build(),
                    ImportRequirement.builder().position("b").applicable("NEIN").build()
                ))
            )
        );

        // act
        service.updateSelectedRequirements(project, phase, data);

        // assert
        verify(requirementServiceMock, times(1)).handleText(eq("DUMMY"), eq("master"), eq("1"), any(), any());
        verify(requirementServiceMock, times(1)).handleText("DUMMY", "master", "1", "a", "Dies ist der neue Text");

    }

    @Test
    void updateAusgewaehlteAnforderungen_AnforderungenMitGueltigemStatusUndLeererTextanpassung_AnforderungenOhneTextWerdenVerarbeitet() {
        // arrange
        String project = "DUMMY";
        String phase = "master";
        byte[] data = "Filereader wird gemockt. Kein parsen einer File".getBytes(UTF_8);

        given(tailoringAnforderungFileReaderMock.apply(data)).willReturn(Map.ofEntries(
                new AbstractMap.SimpleEntry<>("1", asList(
                    ImportRequirement.builder().position("a").applicable("JA").text("").build(),
                    ImportRequirement.builder().position("b").applicable("NEIN").build()
                ))
            )
        );

        // act
        service.updateSelectedRequirements(project, phase, data);

        // assert
        verify(requirementServiceMock, times(0)).handleText(eq("DUMMY"), eq("master"), eq("1"), any(), any());

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