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
    void getAnforderung_ProjektNichtVorhanden_EmptyErgebniss() {
        // arrange
        when(projectRepositoryMock.findByIdentifier("SAMPLE")).thenReturn(null);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getAnforderung_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getRequirement(null, "master1", "1.2.1", "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderung_PhaseNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getRequirement("DUMMY", null, "1.2.1", "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderung_KapitelNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getRequirement("DUMMY", "master", null, "b"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderung_PositionNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getRequirement("DUMMY", "master", "1.2.1", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAnforderung_PhaseNichtVorhanden_EmptyErgebniss() {
        // arrange
        ProjectEntity projekt = ProjectEntity.builder()
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
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getAnforderung_KapitelNichtVorhanden_EmptyErgebniss() {
        // arrange
        ProjectEntity projekt = ProjectEntity.builder()
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
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master", "1.1.2", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getAnforderung_AnforderungNichtVorhanden_EmptyErgebniss() {
        // arrange
        ProjectEntity projekt = ProjectEntity.builder()
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
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master", "1.1", "b");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getAnforderung() {
        // arrange
        ProjectEntity projekt = ProjectEntity.builder()
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
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        // act
        Optional<TailoringRequirement> actual = repository.getRequirement("SAMPLE", "master1", "1.2.1", "b");

        // assert
        assertThat(actual).isNotNull();
    }


    @Test
    void updateAnforderung_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateRequirement(null, "master1", "1.2.1", TailoringRequirement.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAnforderung_PhaseNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateRequirement("DUMMY", null, "1.2.1", TailoringRequirement.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAnforderung_KapitelNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateRequirement("DUMMY", "master", null, TailoringRequirement.builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAnforderung_AnforderungNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateRequirement("DUMMY", "master", "1.2.1", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAnforderung_GruppeNichtVorhanden_EmptyErgebnis() {
        when(projectRepositoryMock.findByIdentifier("SAMPLE")).thenReturn(null);

        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("a")
            .build();

        // act
        Optional<TailoringRequirement> actual = repository.updateRequirement("SAMPLE", "master", "1.1", anforderung);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateAnforderung_AnforderungNichtVorhanden_EmptyErgebnis() {
        // arrange
        ProjectEntity projekt = ProjectEntity.builder()
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
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);


        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("b")
            .build();

        // act
        Optional<TailoringRequirement> actual = repository.updateRequirement("SAMPLE", "master", "1.1", anforderung);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateAnforderung_AnforderungVorhanden_WerteWurdenUebernommen() {
        // arrange
        TailoringRequirementEntity anforderungToUpdate = TailoringRequirementEntity.builder()
            .position("a")
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
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
                                                anforderungToUpdate
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
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        TailoringRequirement anforderung = TailoringRequirement.builder()
            .position("a")
            .build();
        given(mapperMock.toDomain(anforderungToUpdate))
            .willReturn(anforderung);

        // act
        Optional<TailoringRequirement> actual = repository.updateRequirement("SAMPLE", "master", "1.1", anforderung);

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, timeout(1))
            .updateRequirement(anforderung, anforderungToUpdate);
    }


    @Test
    void getKapitel_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getChapter(null, "master", "1.2.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_PhaseNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getChapter("DUMMY", null, "1.2.1"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_KapitelNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.getChapter("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void getKapitel_KapitelVorhanden_DomaenenobjektWirdZurueckGegeben() {

        // arrange
        TailoringCatalogChapterEntity gruppe = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        given(mapperMock.toDomain(gruppe))
            .willReturn(Chapter.<TailoringRequirement>builder().build());

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.getChapter("SAMPLE", "master", "1.1");

        // assert
        assertThat(actual).isPresent();
    }

    @Test
    void getKapitel_KapitelNichtVorhanden_EmptyErgebnis() {
        // arrange
        TailoringCatalogChapterEntity gruppe = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        given(mapperMock.toDomain(gruppe))
            .willReturn(Chapter.<TailoringRequirement>builder().build());

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.getChapter("SAMPLE", "master", "1.2");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateKapitel_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateChapter(null, "master", Chapter.<TailoringRequirement>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateKapitel_PhaseNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateChapter("DUMMY", null, Chapter.<TailoringRequirement>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateKapitel_KapitelNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateChapter("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }


    @Test
    void updateKapitel_KapitelNichtVorhanden_EmptyErgebnis() {
        // arrange
        TailoringCatalogChapterEntity gruppe = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("2")
            .build();

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.updateChapter("SAMPLE", "master", chapter);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void updateKapitel_KapitelVorhanden_WerteUebernommen() {
        // arrange
        TailoringCatalogChapterEntity kapitelToUpdate = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                kapitelToUpdate)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1.1")
            .build();

        given(mapperMock.toDomain(kapitelToUpdate))
            .willReturn(chapter);

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.updateChapter("SAMPLE", "master", chapter);

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1))
            .updateChapter(chapter, kapitelToUpdate);
    }

    @Test
    void updateAusgewaehlt_ProjektNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateSelected(null, "master", Chapter.<TailoringRequirement>builder().build()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAusgewaehlt_PhasetNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act

        Throwable actual = catchThrowable(() -> repository.updateSelected("DUMMY", null, Chapter.<TailoringRequirement>builder().build()));
        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void updateAusgewaehlt_GruppeNichtUebergeben_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> repository.updateSelected("DUMMY", "master", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }


    @Test
    void updateAusgewaehlt_KapitelNichtVorhanden_EmptyErgebnis() {
        // arrange
        TailoringCatalogChapterEntity gruppe = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .build()
            ))
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("2")
            .build();

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.updateSelected("SAMPLE", "master", chapter);

        // assert
        assertThat(actual).isEmpty();

    }

    @Test
    void updateAusgewaehlt_KapitelVorhanden_AuswahlUebernommen() {
        // arrange
        TailoringRequirementEntity anforderungAToUpdate = TailoringRequirementEntity.builder()
            .position("a")
            .selected(Boolean.TRUE)
            .build();
        TailoringRequirementEntity anforderungBToUpdate = TailoringRequirementEntity.builder()
            .position("b")
            .selected(Boolean.FALSE)
            .build();

        TailoringCatalogChapterEntity gruppe = TailoringCatalogChapterEntity.builder()
            .number("1")
            .chapters(asList(
                TailoringCatalogChapterEntity.builder()
                    .number("1.1")
                    .requirements(asList(
                        anforderungAToUpdate,
                        anforderungBToUpdate
                    ))
                    .build()
            ))
            .build();
        ProjectEntity projekt = ProjectEntity.builder()
            .identifier("SAMPLE")
            .tailorings(asList(
                TailoringEntity.builder()
                    .id(2L)
                    .name("master")
                    .phase(ZERO)
                    .catalog(TailoringCatalogEntity.builder()
                        .toc(TailoringCatalogChapterEntity.builder()
                            .chapters(asList(
                                gruppe)
                            )
                            .build())
                        .build())
                    .build()
            ))
            .build();
        given(projectRepositoryMock.findByIdentifier("SAMPLE"))
            .willReturn(projekt);

        TailoringRequirement anforderungA = TailoringRequirement.builder()
            .position("a")
            .selected(Boolean.TRUE)
            .build();
        TailoringRequirement anforderungB = TailoringRequirement.builder()
            .position("b")
            .selected(Boolean.TRUE)
            .build();
        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1.1")
            .requirements(asList(
                anforderungA,
                anforderungB
            ))
            .build();

        given(mapperMock.toDomain(gruppe))
            .willReturn(chapter);

        // act
        Optional<Chapter<TailoringRequirement>> actual = repository.updateSelected("SAMPLE", "master", chapter);

        // assert
        assertThat(actual).isNotEmpty();

        verify(mapperMock, times(1))
            .updateRequirement(anforderungA, anforderungAToUpdate);

        verify(mapperMock, times(1))
            .updateRequirement(anforderungB, anforderungBToUpdate);

    }
}
