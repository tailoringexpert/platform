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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static eu.tailoringexpert.domain.ProjectState.COMPLETED;
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
    BaseCatalogRepository baseCatalogRepositoryMock;
    JPAProjectServiceRepositoryMapper mapperMock;
    JPAProjectServiceRepository repository;

    @BeforeEach
    void setup() {
        this.projectRepositoryMock = mock(ProjectRepository.class);
        this.baseCatalogRepositoryMock = mock(BaseCatalogRepository.class);
        this.mapperMock = mock(JPAProjectServiceRepositoryMapper.class);
        this.repository = new JPAProjectServiceRepository(
            this.mapperMock,
            this.projectRepositoryMock,
            this.baseCatalogRepositoryMock
        );
    }

    @Test
    void getBaseCatalog_BaseCatalogNotExists_NullReturned() {
        // arrange
        given(baseCatalogRepositoryMock.findByVersion("8.2.1")).willReturn(null);
        given(mapperMock.toDomain((BaseCatalogEntity) null)).willReturn(null);

        // act
        Catalog<BaseRequirement> actual = repository.getBaseCatalog("8.2.1");

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void getBaseCatalog_BasrCatalogExists_CatalogReturned() {
        // arrange
        BaseCatalogEntity baseCatalog = BaseCatalogEntity.builder().build();

        given(baseCatalogRepositoryMock.findByVersion("8.2.1")).willReturn(baseCatalog);
        given(mapperMock.toDomain(baseCatalog)).willReturn(Catalog.<BaseRequirement>builder().build());

        // act
        Catalog<BaseRequirement> actual = repository.getBaseCatalog("8.2.1");

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void createProject_ProjectNotNull_ProjectCreated() {
        // arrange
        Project project = Project.builder().build();
        ProjectEntity projectToSave = ProjectEntity.builder().build();

        given(mapperMock.createProject(project)).willReturn(projectToSave);
        given(mapperMock.toDomain(projectToSave)).willReturn(Project.builder().build());
        given(projectRepositoryMock.save(projectToSave)).willReturn(projectToSave);

        // act
        Project actual = repository.createProject(project);

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void deleteProject_ProjectExists_ProjectDeleted() {
        // arrange
        given(projectRepositoryMock.deleteByIdentifier("SAMPLE")).willReturn(1l);

        // act
        boolean actual = repository.deleteProject("SAMPLE");

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void deleteProject_ProjectNotExists_NoProjectDeleted() {
        // arrange
        given(projectRepositoryMock.deleteByIdentifier("SAMPLE")).willReturn(0l);

        // act
        boolean actual = repository.deleteProject("SAMPLE");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void getProject_ProjectNotExists_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(null);
        given(mapperMock.toDomain((ProjectEntity) null)).willReturn(null);

        // act
        Optional<Project> actual = repository.getProject("SAMPLE");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getProject_ProjectExists_ProjectReturned() {
        // arrange
        ProjectEntity projectEntity = ProjectEntity.builder().build();

        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(projectEntity);
        given(mapperMock.toDomain(projectEntity)).willReturn(Project.builder().build());

        // act
        Optional<Project> actual = repository.getProject("SAMPLE");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void addTailoring_ProjectExists_TailoringAdded() {
        // arrange
        ProjectEntity projectEntity = ProjectEntity.builder().tailorings(new ArrayList<>()).build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(projectEntity);

        Tailoring tailoring = Tailoring.builder()
            .catalog(Catalog.<TailoringRequirement>builder().version("8.2.1").build())
            .build();
        TailoringEntity tailoringToAdd = TailoringEntity.builder().build();

        given(mapperMock.toEntity(tailoring)).willReturn(tailoringToAdd);
        given(mapperMock.toDomain(tailoringToAdd)).willReturn(Tailoring.builder().build());

        // act
        Optional<Tailoring> actual = repository.addTailoring("SAMPLE", tailoring);

        // assert
        assertThat(actual).isPresent();
        assertThat(projectEntity.getTailorings()).contains(tailoringToAdd);
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

        given(mapperMock.getProjectInformationen(any(ProjectEntity.class))).willReturn(ProjectInformation.builder().build());

        // act
        Collection<ProjectInformation> actual = repository.getProjectInformations();

        // assert
        assertThat(actual).hasSize(3);
    }

    @Test
    void getScreeningSheet_ProjectNotExists_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findByIdentifier(anyString())).willReturn(null);

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("SAMPLE");

        // assert
        assertThat(actual).isEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier("SAMPLE");
        verify(mapperMock, times(0)).getScreeningSheet(any());
    }

    @Test
    void getScreeningSheet_ProjectExists_ScreeningSheetReturned() {
        // arrange
        ScreeningSheetEntity screeningSheet = ScreeningSheetEntity.builder().build();
        ProjectEntity project = ProjectEntity.builder()
            .screeningSheet(screeningSheet)
            .build();
        given(projectRepositoryMock.findByIdentifier(anyString())).willReturn(project);
        given(mapperMock.getScreeningSheet(screeningSheet)).willReturn(ScreeningSheet.builder().build());

        // act
        Optional<ScreeningSheet> actual = repository.getScreeningSheet("SAMPLE");

        // assert
        assertThat(actual).isNotEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier("SAMPLE");
        verify(mapperMock, times(1)).getScreeningSheet(screeningSheet);
    }

    @Test
    void getProjectInformation_IdentifierNull_EmptyReturned() {
        // arrange
        String project = null;

        ProjectEntity entity = null;
        given(projectRepositoryMock.findByIdentifier(project)).willReturn(entity);
        given(mapperMock.getProjectInformationen(entity)).willReturn(null);

        // act
        Optional<ProjectInformation> actual = repository.getProjectInformation(project);

        // assert
        assertThat(actual).isEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier(project);
        verify(mapperMock, times(1)).getProjectInformationen(entity);

    }

    @Test
    void getProjectInformation_IdentifierExists_ProjectInformationReturned() {
        // arrange
        String project = "SAMPLE";

        ProjectEntity entity = ProjectEntity.builder().identifier("SAMPLE").build();
        given(projectRepositoryMock.findByIdentifier(project)).willReturn(entity);

        ProjectInformation projectInformation = ProjectInformation.builder().identifier("SAMPLE").build();
        given(mapperMock.getProjectInformationen(entity)).willReturn(projectInformation);

        // act
        Optional<ProjectInformation> actual = repository.getProjectInformation(project);

        // assert
        assertThat(actual).isNotEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier(project);
        verify(mapperMock, times(1)).getProjectInformationen(entity);

    }

    @Test
    void getScreeningSheetFile_IdentifierNull_EmptyReturned() {
        // arrange
        String project = null;

        ProjectEntity entity = null;
        given(projectRepositoryMock.findByIdentifier(project)).willReturn(entity);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile(project);

        // assert
        assertThat(actual).isEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier(project);

    }

    @Test
    void getScreeningSheetFile_ProjectExists_FileReturned() {
        // arrange
        String project = "SAMPLE";

        ProjectEntity entity = ProjectEntity.builder()
            .identifier("SAMPLE")
            .screeningSheet(ScreeningSheetEntity.builder()
                .id(4711L)
                .data("Hi there".getBytes(UTF_8))
                .build())
            .build();
        given(projectRepositoryMock.findByIdentifier(project)).willReturn(entity);

        // act
        Optional<byte[]> actual = repository.getScreeningSheetFile(project);

        // assert
        assertThat(actual).isNotEmpty();
        verify(projectRepositoryMock, times(1)).findByIdentifier(project);

    }

    @Test
    void updateState_ProjectNotExists_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(null);

        // act
        Optional<ProjectInformation> actual = repository.updateState("SAMPLE", COMPLETED);

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(0)).getProjectInformationen(any(ProjectEntity.class));
    }

    @Test
    void updateState_ProjectExists_UpdatedInformationReturned() {
        // arrange
        ProjectEntity project = ProjectEntity.builder().build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);
        given(mapperMock.getProjectInformationen(project)).willReturn(ProjectInformation.builder().build());

        // act
        Optional<ProjectInformation> actual = repository.updateState("SAMPLE", COMPLETED);

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1)).getProjectInformationen(project);
    }

}
