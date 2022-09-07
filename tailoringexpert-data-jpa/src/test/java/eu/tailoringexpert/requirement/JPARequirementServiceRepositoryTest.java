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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringRequirementEntity;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringCatalogEntity;
import eu.tailoringexpert.domain.TailoringCatalogChapterEntity;
import eu.tailoringexpert.repository.ProjectRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Log4j2
class JPARequirementServiceRepositoryTest {

    ProjectRepository projectRepositoryMock;
    JPARequirementServiceRepositoryMapper mapperMock;
    JPARequirementServiceRepository repository;

    @BeforeEach
    void setup() {
        this.projectRepositoryMock = mock(ProjectRepository.class);
        this.mapperMock = mock(JPARequirementServiceRepositoryMapper.class);
        this.repository = new JPARequirementServiceRepository(
            this.mapperMock,
            this.projectRepositoryMock
        );
    }

    @Test
    void getRequirement_ProjectNotExists_EmptyReturned() {
        // arrange
        when(projectRepositoryMock.findByIdentifier("SAMPLE")).thenReturn(null);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getRequirement_ProjektNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getRequirement(null, "master1", "1.2.1", "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getRequirement_TailoringNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getRequirement("DUMMY", null, "1.2.1", "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getRequirement_ChapterNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getRequirement("DUMMY", "master", null, "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getRequirement_PositionNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getRequirement("DUMMY", "master", "1.2.1", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getRequirement_TailoringNotExists_EmptyReturned() {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder().build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getRequirement_ChapterNotExists_EmptyReturned() {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                TailoringCatalogChapterEntity.builder()
                                    .number("1")
                                    .chapters(asList(
                                        TailoringCatalogChapterEntity.builder()
                                            .number("1.1")
                                            .chapters(asList(
                                                TailoringCatalogChapterEntity.builder()
                                                    .number("1.1.1")
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master", "1.1.2", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getRequirement_RequirementNoExists_EmptyReturned() {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                TailoringCatalogChapterEntity.builder()
                                    .number("1")
                                    .chapters(asList(
                                        TailoringCatalogChapterEntity.builder()
                                            .number("1.1")
                                            .requirements(asList(
                                                TailoringRequirementEntity.builder()
                                                    .position("a")
                                                    .build()
                                            ))
                                            .chapters(asList(
                                                TailoringCatalogChapterEntity.builder()
                                                    .number("1.1.1")
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master", "1.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getRequirement_RequirementExists_RequirementReturned() {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                TailoringCatalogChapterEntity.builder()
                                    .number("1")
                                    .chapters(asList(
                                        TailoringCatalogChapterEntity.builder()
                                            .number("1.1")
                                            .chapters(asList(
                                                TailoringCatalogChapterEntity.builder()
                                                    .number("1.1.1")
                                                    .build()
                                            ))
                                            .build(),
                                        TailoringCatalogChapterEntity.builder()
                                            .number("1.2")
                                            .chapters(asList(
                                                TailoringCatalogChapterEntity.builder()
                                                    .number("1.2.1")
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build(),
                TailoringEntity.builder()
                    .id(3L)
                    .name("master1")
                    .phase(E)
                    .phase(F)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                TailoringCatalogChapterEntity.builder()
                                    .number("1")
                                    .chapters(asList(
                                        TailoringCatalogChapterEntity.builder()
                                            .number("1.1")
                                            .chapters(asList(
                                                TailoringCatalogChapterEntity.builder()
                                                    .number("1.1.1")
                                                    .build()
                                            ))
                                            .build(),
                                        TailoringCatalogChapterEntity.builder()
                                            .number("1.2")
                                            .chapters(asList(
                                                TailoringCatalogChapterEntity.builder()
                                                    .number("1.2.1")
                                                    .requirements(asList(
                                                        TailoringRequirementEntity.builder()
                                                            .position("a")
                                                            .build(),
                                                        TailoringRequirementEntity.builder()
                                                            .position("b")
                                                            .build()))
                                                    .build()
                                            ))
                                            .build()))
                                    .build()))
                            .build())
                        .build())
                    .build()))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isNotNull();
    }


    @Test
    void updateRequirement_ProjectNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateRequirement(null, "master1", "1.2.1", TailoringRequirement.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateRequirement_TailoringNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateRequirement("DUMMY", null, "1.2.1", TailoringRequirement.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateRequirement_ChapterNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateRequirement("DUMMY", "master", null, TailoringRequirement.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateRequirement_RequirementNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateRequirement("DUMMY", "master", "1.2.1", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateRequirement_ChapterNotExists_EmptyReturned() {
        // arrange
        when(projectRepositoryMock.findByIdentifier("SAMPLE")).thenReturn(null);

        TailoringRequirement requirement = TailoringRequirement.builder()
            .position("a")
            .build();

        // act
        Optional<TailoringRequirement> actual = repository.updateRequirement("SAMPLE", "master", "1.1", requirement);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateRequirement_RequirementNotExists_EmptyReturned() {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                TailoringCatalogChapterEntity.builder()
                                    .number("1")
                                    .chapters(asList(
                                        TailoringCatalogChapterEntity.builder()
                                            .number("1.1")
                                            .requirements(asList(
                                                TailoringRequirementEntity.builder()
                                                    .position("a")
                                                    .build()
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);


        TailoringRequirement requirement = TailoringRequirement.builder()
            .position("b")
            .build();

        // act
        Optional<TailoringRequirement> actual = repository.updateRequirement("SAMPLE", "master", "1.1", requirement);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateRequirement_RequirementExists_RequirementUpdated() {
        // arrange
        TailoringRequirementEntity requirementToUpdate = TailoringRequirementEntity.builder()
            .position("a")
            .build();
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                TailoringCatalogChapterEntity.builder()
                                    .number("1")
                                    .chapters(asList(
                                        TailoringCatalogChapterEntity.builder()
                                            .number("1.1")
                                            .requirements(asList(
                                                requirementToUpdate
                                            ))
                                            .build()
                                    ))
                                    .build())
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        TailoringRequirement requirement = TailoringRequirement.builder()
            .position("a")
            .build();
        given(mapperMock.toDomain(requirementToUpdate)).willReturn(requirement);

        // act
        Optional<TailoringRequirement> actual = repository.updateRequirement("SAMPLE", "master", "1.1", requirement);

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, timeout(1)).updateRequirement(requirement, requirementToUpdate);
    }


    @Test
    void getChapter_ProjectNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getChapter(null, "master", "1.2.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getChapter_TailoringNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getChapter("DUMMY", null, "1.2.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getChapter_ChapterNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getChapter("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getChapter_ChapterExists_MappedDomainObjectReturned() {

        // arrange
        TailoringCatalogChapterEntity chapter = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                chapter)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        given(mapperMock.toDomain(chapter)).willReturn(Chapter.<TailoringRequirement>builder().build());

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.getChapter("SAMPLE", "master", "1.1");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void getChapter_ChapterNotExists_EmptyReturned() {
        // arrange
        TailoringCatalogChapterEntity chapter = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                chapter)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        given(mapperMock.toDomain(chapter)).willReturn(Chapter.<TailoringRequirement>builder().build());

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.getChapter("SAMPLE", "master", "1.2");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateChapter_ProjectNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateChapter(null, "master", Chapter.<TailoringRequirement>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateChapter_TailoringNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateChapter("DUMMY", null, Chapter.<TailoringRequirement>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateChapter_ChapterNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateChapter("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }


    @Test
    void updateChapter_ChapterNotExists_EmptyReturned() {
        // arrange
        TailoringCatalogChapterEntity chapterToUpdate = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                chapterToUpdate)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("2")
            .build();

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.updateChapter("SAMPLE", "master", chapter);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateChapter_ChapterExists_ChapterUpdated() {
        // arrange
        TailoringCatalogChapterEntity chapterToUpdate = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                chapterToUpdate)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1.1")
            .build();

        given(mapperMock.toDomain(chapterToUpdate)).willReturn(chapter);

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.updateChapter("SAMPLE", "master", chapter);

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1)).updateChapter(chapter, chapterToUpdate);
    }

    @Test
    void updateSelected_ProjectNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateSelected(null, "master", Chapter.<TailoringRequirement>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateSelected_TailoringNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateSelected("DUMMY", null, Chapter.<TailoringRequirement>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateSelected_ChapterNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateSelected("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }


    @Test
    void updateSelected_ChapterNotExists_EmptyReturned() {
        // arrange
        TailoringCatalogChapterEntity projectChapter = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();

        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                projectChapter)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("2")
            .build();

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.updateSelected("SAMPLE", "master", chapter);

        // assert
        assertThat(actual).isEmpty();

    }

    @Test
    void updateSelected_ChapterExists_SelectionUpdated() {
        // arrange
        TailoringRequirementEntity requirementAToUpdate = TailoringRequirementEntity.builder()
            .position("a")
            .selected(Boolean.TRUE)
            .build();
        TailoringRequirementEntity requirementBToUpdate = TailoringRequirementEntity.builder()
            .position("b")
            .selected(Boolean.FALSE)
            .build();

        TailoringCatalogChapterEntity projectChapter = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .requirements(asList(
                        requirementAToUpdate,
                        requirementBToUpdate
                    ))
                    .build()
            ))
            .build();
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                projectChapter)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE")).willReturn(project);

        TailoringRequirement requirementA = TailoringRequirement.builder()
            .position("a")
            .selected(Boolean.TRUE)
            .build();
        TailoringRequirement requirementB = TailoringRequirement.builder()
            .position("b")
            .selected(Boolean.TRUE)
            .build();
        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1.1")
            .requirements(asList(
                requirementA,
                requirementB
            ))
            .build();
        given(mapperMock.toDomain(projectChapter)).willReturn(chapter);

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.updateSelected("SAMPLE", "master", chapter);

        // assert
        assertThat(actual).isNotEmpty();
        verify(mapperMock, times(1)).updateRequirement(requirementA, requirementAToUpdate);
        verify(mapperMock, times(1)).updateRequirement(requirementB, requirementBToUpdate);

    }
}
