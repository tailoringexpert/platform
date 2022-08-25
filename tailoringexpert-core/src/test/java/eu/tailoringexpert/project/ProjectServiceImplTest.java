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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Identifier;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringState;
import eu.tailoringexpert.tailoring.ScreeningSheetDataProviderSupplier;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import eu.tailoringexpert.tailoring.TailoringService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Optional;

import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
class ProjectServiceImplTest {

    private ProjectServiceImpl service;

    private TailoringService tailoringServiceMock;
    private ScreeningSheetService screeningSheetServiceMock;
    private ProjectServiceRepository repositoryMock;

    @BeforeEach
    void setup() {
        this.repositoryMock = mock(ProjectServiceRepository.class);
        this.tailoringServiceMock = mock(TailoringService.class);
        this.screeningSheetServiceMock = mock(ScreeningSheetService.class);
        this.service = new ProjectServiceImpl(
            repositoryMock,
            screeningSheetServiceMock,
            tailoringServiceMock
        );
    }

    @Test
    void ProjectServiceImpl_ProjectServiceRepositoryNotProvided_NullPointerExceptionIsThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> new ProjectServiceImpl(
            null,
            mock(ScreeningSheetService.class),
            mock(TailoringService.class)));

        //assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ProjectServiceImpl_ScreeningServiceNotProvided_NullPointerExceptionIsThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> new ProjectServiceImpl(
            mock(ProjectServiceRepository.class),
            null,
            mock(TailoringService.class)));

        //assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void ProjectServiceImpl_TailoringServiceNotProvided_NullPointerExceptionIsThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> new ProjectServiceImpl(
            mock(ProjectServiceRepository.class),
            mock(ScreeningSheetService.class),
            null));

        //assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createProject_AllParameterValidProvided_ProjectIsCreated() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder()
                .chapters(asList(Chapter.<BaseRequirement>builder()
                    .name("General")
                    .requirements(asList(
                        BaseRequirement.builder()
                            .text("First Requirement")
                            .position("a")
                            .identifiers(asList(
                                Identifier.builder()
                                    .type("Q")
                                    .level(4)
                                    .limitations(asList("SAT", "LEO"))
                                    .build()
                            ))
                            .build()))
                    .build()))
                .build())
            .build();
        given(repositoryMock.getBaseCatalog(anyString())).willReturn(catalog);

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .data(data)
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kurzname.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .selectionVector(selectionVector)
            .build();
        given(screeningSheetServiceMock.createScreeningSheet(any())).willReturn(screeningSheet);

        given(tailoringServiceMock.createTailoring(any(), any(), eq(screeningSheet), any(), eq(catalog))).willReturn(Tailoring.builder()
            .screeningSheet(screeningSheet)
            .phases(asList())
            .state(TailoringState.CREATED)
            .build());

        given(repositoryMock.createProject( any(Project.class))).willReturn(Project.builder()
            .identifier("SAMPLE")
            .tailoring(Tailoring.builder()
                .screeningSheet(screeningSheet)
                .build())
            .build());


        // act
        CreateProjectTO actual = service.createProject("1", data, selectionVector);

        // assert
        assertThat(actual.getProject()).isNotBlank();

        log.info(actual.toString());
    }

    @Test
    void deleteProjekt_ProjectExists_ProjectIsDeleted() {
        // arrange
        given(repositoryMock.getProject("SAMPLE"))
            .willAnswer(invocation -> of(
                Project.builder()
                    .identifier(invocation.getArgument(0))
                    .build())
            );
        given(repositoryMock.deleteProject("SAMPLE")).willReturn(true);

        // act
        boolean actual = service.deleteProject("SAMPLE");

        // assert
        verify(repositoryMock, times(1))
            .deleteProject("SAMPLE");
        assertThat(actual).isTrue();
    }

    @Test
    void deleteProjekt_ProjectNotExists_NoRepositoreyDeletionCall() {
        // arrange
        given(repositoryMock.getProject("SAMPLE")).willReturn(empty());

        // act
        boolean actual = service.deleteProject("SAMPLE");

        // assert
        verify(repositoryMock, times(0))
            .deleteProject("SAMPLE");
        assertThat(actual).isFalse();
    }

    @Test
    void addTailoring_ProjectNotExists_TailoringNotAdded() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProject("DUMMY")).willReturn(empty());

        // act
        Optional<Tailoring> actual = service.addTailoring("DUMMY", "8.2.1", data, SelectionVector.builder().build());

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0)).getProject("8.2.1");
        verify(repositoryMock, times(0)).addTailoring(anyString(), any());
    }

    @Test
    void addTailoring_ScreeningSheetOfWrongProject_TailoringIsNotAdded() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProject("DUMMY")).willReturn(of(Project.builder().build()));

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category("kuerzel")
                    .value("ANDERES PROJEKT")
                    .build()
            ))
            .build();

        given(screeningSheetServiceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        // act
        Optional<Tailoring> actual = service.addTailoring("DUMMY", "8.2.1", data, SelectionVector.builder().build());

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getBaseCatalog("8.2.1");
        verify(tailoringServiceMock, times(0)).createTailoring(any(), any(), any(), any(), any());
        verify(repositoryMock, times(0)).addTailoring(anyString(), any());
    }

    @Test
    void addTailoring_BaseCatalogNotExists_TailoringNotAdded() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProject("DUMMY")).willReturn(of(Project.builder().build()));
        given(repositoryMock.getBaseCatalog("8.2.1")).willReturn(null);

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category("kuerzel")
                    .value("ANDERES PROJEKT")
                    .build()
            ))
            .build();

        given(screeningSheetServiceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        // act
        Optional<Tailoring> actual = service.addTailoring("DUMMY", "8.2.1", data, SelectionVector.builder().build());

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(1)).getBaseCatalog("8.2.1");
        verify(tailoringServiceMock, times(0)).createTailoring(any(), any(), any(), any(), any());
        verify(repositoryMock, times(0)).addTailoring(anyString(), any());
    }

    @Test
    void addTailoring_FirstTailoring_TailoringMasterIsAdded() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProject("SAMPLE"))
            .willAnswer(invocation -> of(
                Project.builder()
                    .identifier(invocation.getArgument(0))
                    .build())
            );

        given(repositoryMock.getBaseCatalog("8.2.1")).willReturn(Catalog.<BaseRequirement>builder().build());

        ArgumentCaptor<String> projektPhaseNameCaptor = ArgumentCaptor.forClass(String.class);
        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(ScreeningSheetParameter.builder().category(ScreeningSheetDataProviderSupplier.Kuerzel.getName()).value("SAMPLE").build()))
            .selectionVector(SelectionVector.builder().build())
            .build();
        given(screeningSheetServiceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        given(tailoringServiceMock.createTailoring(projektPhaseNameCaptor.capture(), any(), eq(screeningSheet), any(), any())).willAnswer(invocation ->
            Tailoring.builder()
                .name(invocation.getArgument(0))
                .identifier(invocation.getArgument(1))
                .screeningSheet(invocation.getArgument(2))
                .build()
        );

        given(repositoryMock.addTailoring(eq("SAMPLE"), any(Tailoring.class)))
            .willReturn(of(Tailoring.builder().build()));


        // act
        Optional<Tailoring> actual = service.addTailoring("SAMPLE", "8.2.1", data, screeningSheet.getSelectionVector());

        // assert
        verify(repositoryMock, times(1)).getBaseCatalog("8.2.1");
        assertThat(projektPhaseNameCaptor.getValue()).isEqualTo("master");
        assertThat(actual).isPresent();
    }

    @Test
    void addTailoring_OneTailoringAlreadyExisting_TailoringMaster1IsAdded() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProject("SAMPLE"))
            .willAnswer(invocation -> of(
                Project.builder()
                    .identifier(invocation.getArgument(0))
                    .tailorings(asList(
                        Tailoring.builder()
                            .name("master")
                            .identifier("1000")
                            .build()
                    ))
                    .build())
            );
        given(repositoryMock.getBaseCatalog("8.2.1")).willReturn(Catalog.<BaseRequirement>builder().build());

        SelectionVector selectionVector = SelectionVector.builder().build();
        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(ScreeningSheetParameter.builder().category(ScreeningSheetDataProviderSupplier.Kuerzel.getName()).value("SAMPLE").build()))
            .selectionVector(selectionVector)
            .build();
        given(screeningSheetServiceMock.createScreeningSheet(data)).willReturn(screeningSheet);

        ArgumentCaptor<String> projektPhaseNameCaptor = ArgumentCaptor.forClass(String.class);
        given(tailoringServiceMock.createTailoring(projektPhaseNameCaptor.capture(), any(), any(), any(), any())).willAnswer(invocation ->
            Tailoring.builder()
                .name(invocation.getArgument(0))
                .screeningSheet(screeningSheet)
                .signatures(Collections.emptyList())
                .build()
        );

        given(repositoryMock.addTailoring(eq("SAMPLE"), any(Tailoring.class)))
            .willReturn(of(Tailoring.builder().build()));

        // act
        Optional<Tailoring> actual = service.addTailoring("SAMPLE", "8.2.1", data, selectionVector);

        // assert
        assertThat(projektPhaseNameCaptor.getValue()).isEqualTo("master1");
        assertThat(actual).isPresent();

    }

    @Test
    void copyProjekt_ProjectNotExisting_ProjectcopyNotCreated() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(repositoryMock.getProject("DUMMY")).willReturn(empty());

        // act
        Optional<Project> actual = service.copyProject("DUMMY", data);

        // assert
        assertThat(actual).isEmpty();
        verify(repositoryMock, times(0)).createProject(any());
    }

    @Test
    void copyProjekt_ProjectExists_ProjectcopyCreated() throws IOException {
        // arrange
        byte[] data = newInputStream(get("src/test/resources/screeningsheet.pdf")).readAllBytes();

        given(repositoryMock.getProject("SAMPLE"))
            .willAnswer(invocation -> of(
                Project.builder()
                    .identifier(invocation.getArgument(0))
                    .tailorings(asList(
                        Tailoring.builder()
                            .name("master")
                            .phases(asList(
                                Phase.ZERO,
                                Phase.A
                            ))
                            .build(),
                        Tailoring.builder()
                            .name("master1")
                            .phases(asList(
                                Phase.B,
                                Phase.C,
                                Phase.D
                            ))
                            .build()
                    ))
                    .build())
            );

        given(screeningSheetServiceMock.createScreeningSheet(data))
            .willReturn(ScreeningSheet.builder()
                .parameters(asList(ScreeningSheetParameter.builder().category(ScreeningSheetDataProviderSupplier.Kuerzel.getName()).value("H3SAT").build()))
                .build());

        ArgumentCaptor<Project> projektKopieCaptor = ArgumentCaptor.forClass(Project.class);
        given(repositoryMock.createProject(projektKopieCaptor.capture()))
            .willAnswer(invocation -> invocation.getArgument(0));

        // act
        Optional<Project> actual = service.copyProject("SAMPLE", data);

        // assert
        assertThat(actual).isPresent();
        assertThat(projektKopieCaptor.getValue().getTailorings()).hasSize(2);
    }
}
