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
package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringState;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;

import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static eu.tailoringexpert.domain.ProjectState.ONGOING;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringJUnitConfig(classes = {DBConfiguration.class})
@Transactional
class ProjectRepositoryTest {

    @Autowired
    BaseCatalogRepository baseCatalogRepository;

    @Autowired
    ProjectRepository repository;

    @Test
    void save_ProjectEntityValid_ProjectSaved() throws IOException {
        // arrange

        // act
        ProjectEntity actual = repository.save(ProjectEntity.builder()
            .identifier("SAMPLE")
            .state(ONGOING)
            .tailorings(Arrays.asList(
                TailoringEntity.builder()
                    .phase(ZERO)
                    .build(),
                TailoringEntity.builder()
                    .phase(E)
                    .phase(F)
                    .build()
            ))
            .build());

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(repository.findByIdentifier("SAMPLE")).isNotNull();
    }

    @Test
    void findByIdentifier_ProjectExists_ProjectReturned() throws IOException {
        // arrange
        repository.save(ProjectEntity.builder().identifier("SAMPLE").state(ONGOING).build());
        repository.save(ProjectEntity.builder().identifier("SAMPLE2").state(ONGOING).build());

        // act
        ProjectEntity actual = repository.findByIdentifier("SAMPLE2");

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getIdentifier()).isEqualTo("SAMPLE2");
    }

    @Test
    void findTailoringe_2TailoringExists_RequestedTailoringReturned() throws IOException {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .state(ONGOING)
            .tailorings(Arrays.asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                TailoringEntity.builder()
                    .name("master1")
                    .build()
            ))
            .build();
        repository.save(project);

        // act
        TailoringEntity actual = repository.findTailoring("SAMPLE", "master1");

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void deleteByIdentifier_ProjectExists_ProjectDeleted() throws IOException {
        // arrange
        repository.save(ProjectEntity.builder().identifier("SAMPLE").state(ONGOING).build());
        repository.save(ProjectEntity.builder().identifier("SAMPLE2").state(ONGOING).build());

        // act
        Long actual = repository.deleteByIdentifier("SAMPLE");

        // assert
        assertThat(actual)
            .isNotNull()
            .isEqualTo(1);
        assertThat(repository.findByIdentifier("SAMPLE")).isNull();
    }

    @Test
    void existsTailoring_TailoringExists_TrueReturned() throws IOException {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .state(ONGOING)
            .tailorings(Arrays.asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                TailoringEntity.builder()
                    .name("master1")
                    .build()
            ))
            .build();
        repository.save(project);

        // act
        boolean actual = repository.existsTailoring("SAMPLE", "master1");

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void existsTailoring_TailoringNotExists_TrueReturned() throws IOException {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .state(ONGOING)
            .tailorings(Arrays.asList(
                TailoringEntity.builder()
                    .name("master")
                    .build(),
                TailoringEntity.builder()
                    .name("master1")
                    .build()
            ))
            .build();
        repository.save(project);

        // act
        boolean actual = repository.existsTailoring("SAMPLE_1", "master1");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void findTailoringState_TailoringExists_StateReturned() throws IOException {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .state(ONGOING)
            .tailorings(Arrays.asList(
                TailoringEntity.builder()
                    .name("master")
                    .state(TailoringState.CREATED)
                    .build(),
                TailoringEntity.builder()
                    .name("master1")
                    .state(TailoringState.AGREED)
                    .build()
            ))
            .build();
        repository.save(project);

        // act
        TailoringState actual = repository.findTailoringState("SAMPLE", "master1");

        // assert
        assertThat(actual).isEqualTo(TailoringState.AGREED);
    }

    @Test
    void findTailoringState_TailoringNotExists_TrueReturned() throws IOException {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .state(ONGOING)
            .tailorings(Arrays.asList(
                TailoringEntity.builder()
                    .name("master")
                    .state(TailoringState.CREATED)
                    .build(),
                TailoringEntity.builder()
                    .name("master1")
                    .state(TailoringState.AGREED)
                    .build()
            ))
            .build();
        repository.save(project);

        // act
        TailoringState actual = repository.findTailoringState("SAMPLE_1", "master1");

        // assert
        assertThat(actual).isNull();
    }

}
