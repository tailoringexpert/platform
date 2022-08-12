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

import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.ProjectInformation;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetEntity;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPAProjectServiceRepositoryTest {

    ProjectRepository projectRepositoryMock;
    BaseCatalogRepository katalogDefinitionRepositoryMock;
    JPAProjectServiceRepositoryMapper mapperMock;
    JPAProjectServiceRepository repository;

    @BeforeEach
    void setup() {
        this.projectRepositoryMock = mock(ProjectRepository.class);
        this.katalogDefinitionRepositoryMock = mock(BaseCatalogRepository.class);
        this.mapperMock = mock(JPAProjectServiceRepositoryMapper.class);
        this.repository = new JPAProjectServiceRepository(
            this.mapperMock,
            this.projectRepositoryMock,
            this.katalogDefinitionRepositoryMock
        );
    }

    @Test
    void getKatalog_KatalogVersionNichtVorhanden_NullErgebnis() {
        // arrange
        given(katalogDefinitionRepositoryMock.findByVersion("8.2.1"))
            .willReturn(null);

        given(mapperMock.toDomain((BaseCatalogEntity) null))
            .willReturn(null);

        // act
        Catalog<BaseRequirement> actual = repository.getBaseCatalog("8.2.1");

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getKatalog_KatalogVersionVorhanden_KatalogErgebnis() {
        // arrange
        BaseCatalogEntity katalog = BaseCatalogEntity.builder().build();
        given(katalogDefinitionRepositoryMock.findByVersion("8.2.1"))
            .willReturn(katalog);

        given(mapperMock.toDomain(katalog))
            .willReturn(Catalog.<BaseRequirement>builder().build());

        // act
        Catalog<BaseRequirement> actual = repository.getBaseCatalog("8.2.1");

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void createProjekt_ProjektNichtNull_ProjektAngelegt() {
        // arrange
        Project project = Project.builder().build();
        ProjectEntity projektToSave = ProjectEntity.builder().build();
        given(mapperMock.createProject(project))
            .willReturn(projektToSave);

        given(mapperMock.toDomain(projektToSave))
            .willReturn(Project.builder().build());

        given(projectRepositoryMock.save(projektToSave))
            .willReturn(projektToSave);

        // act
        Project actual = repository.createProject(project);

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void deleteProjekt_ProjektVorhanden_ProjektGeloescht() {
        // arrange
        given(projectRepositoryMock.deleteByIdentifier("SAMPLE"))
            .willReturn(1l);

        // act
        boolean actual = repository.deleteProjekt("SAMPLE");

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void deleteProjekt_ProjektNichtVorhanden_KeinProjektGeloescht() {
        // arrange
        given(projectRepositoryMock.deleteByIdentifier("SAMPLE"))
            .willReturn(0l);

        // act
        boolean actual = repository.deleteProjekt("SAMPLE");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void getProjekt_ProjektNichtVorhanden_EmptyErgebnis() {
        // arrange
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(null);

        given(mapperMock.toDomain((ProjectEntity) null))
            .willReturn(null);

        // act
        Optional<Project> actual = repository.getProject("SAMPLE");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getProjekt_ProjektVorhanden_ProjektErgebnis() {
        // arrange
        ProjectEntity projectEntity = ProjectEntity.builder().build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projectEntity);

        given(mapperMock.toDomain(projectEntity))
            .willReturn(Project.builder().build());

        // act
        Optional<Project> actual = repository.getProject("SAMPLE");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void addProjektPhase_ProjektVorhanden_PhaseHinzugefuegt() {
        // arrange
        ProjectEntity projectEntity = ProjectEntity.builder().build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projectEntity);

        Tailoring tailoring = Tailoring.builder()
            .catalog(Catalog.<TailoringRequirement>builder().version("8.2.1").build())
            .build();
        TailoringEntity projektPhaseToAdd = TailoringEntity.builder().build();
        given(mapperMock.toEntity(tailoring))
            .willReturn(projektPhaseToAdd);

        given(mapperMock.toDomain(projektPhaseToAdd))
            .willReturn(Tailoring.builder().build());

        // act
        Optional<Tailoring> actual = repository.addTailoring("SAMPLE", tailoring);

        // assert
        assertThat(actual).isPresent();
        assertThat(projectEntity.getTailorings()).contains(projektPhaseToAdd);
    }

    @Test
    void getProjektInformationen() {
        // arrange
        given(projectRepositoryMock.findAll())
            .willReturn(Arrays.asList(
                    ProjectEntity.builder()
                        .identifier("SAMPLE")
                        .build(),
                    ProjectEntity.builder()
                        .identifier("H3SAT")
                        .build(),
                    ProjectEntity.builder()
                        .identifier("H4SAT")
                        .build()
                )
            );

        given(mapperMock.getTailoringInformationen(any(ProjectEntity.class)))
            .willReturn(ProjectInformation.builder().build());

        // act
        Collection<ProjectInformation> actual = repository.getProjectInformations();

        // assert
        assertThat(actual).hasSize(3);
    }

    @Test
    void getScreeningSheet_ProjektNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        given(projectRepositoryMock.findByIdentifier(anyString()))
            .willReturn(null);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY");

        // assert
        assertThat(actual).isEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier("DUMMY");
        verify(mapperMock, times(0)).getScreeningSheet(any());
    }

    @Test
    void getScreeningSheet_ProjektVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        ScreeningSheetEntity screeningSheet = ScreeningSheetEntity.builder().build().builder().build();
        ProjectEntity projekt = ProjectEntity.builder()
            .screeningSheet(screeningSheet)
            .build();
        given(projectRepositoryMock.findByIdentifier(anyString()))
            .willReturn(projekt);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("DUMMY");

        // assert
        assertThat(actual).isEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier("DUMMY");
        verify(mapperMock, times(1)).getScreeningSheet(screeningSheet);
    }

    @Test
    void getProjektInformation_KuerzelNull_OptionalEmptyWirdZurueckGegeben() {
        // arrange
        String projekt = null;

        ProjectEntity entity = null;
        given(projectRepositoryMock.findByIdentifier(projekt)).willReturn(entity);
        given(mapperMock.getTailoringInformationen(entity)).willReturn(null);

        // act
        Optional<ProjectInformation> actual = repository.getProjectInformation(projekt);

        // assert
        assertThat(actual).isEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier(projekt);
        verify(mapperMock, times(1)).getTailoringInformationen(entity);

    }

    @Test
    void getProjektInformation_KuerzelVorhanden_OptionalWirdZurueckGegeben() {
        // arrange
        String projekt = "DUMMY";

        ProjectEntity entity = ProjectEntity.builder().identifier("DUMMY").build();
        given(projectRepositoryMock.findByIdentifier(projekt)).willReturn(entity);

        ProjectInformation projectInformation = ProjectInformation.builder().identifier("DUMMY").build();
        given(mapperMock.getTailoringInformationen(entity)).willReturn(projectInformation);

        // act
        Optional<ProjectInformation> actual = repository.getProjectInformation(projekt);

        // assert
        assertThat(actual).isNotEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier(projekt);
        verify(mapperMock, times(1)).getTailoringInformationen(entity);

    }

    @Test
    void getScreeningSheetDatei_KuerzelNull_OptionalEmptyWirdZurueckGegeben() {
        // arrange
        String projekt = null;

        ProjectEntity entity = null;
        given(projectRepositoryMock.findByIdentifier(projekt)).willReturn(entity);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile(projekt);

        // assert
        assertThat(actual).isEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier(projekt);

    }

    @Test
    void getScreeningSheetDatei_KuerzelVorhanden_OptionalWirdZurueckGegeben() {
        // arrange
        String projekt = "DUMMY";

        ProjectEntity entity = ProjectEntity.builder()
            .identifier("DUMMY")
            .screeningSheet(ScreeningSheetEntity.builder()
                .id(4711L)
                .data("Hallo Du".getBytes(UTF_8))
                .build())
            .build();
        given(projectRepositoryMock.findByIdentifier(projekt)).willReturn(entity);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile(projekt);

        // assert
        assertThat(actual).isNotEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier(projekt);

    }

}
