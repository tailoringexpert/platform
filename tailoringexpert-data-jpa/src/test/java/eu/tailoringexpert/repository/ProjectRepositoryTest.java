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
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;

import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringJUnitConfig(classes = {DBConfiguration.class})
@EnableJpaRepositories
@TestPropertySource("classpath:h2.properties")
@EnableTransactionManagement
@DirtiesContext
class ProjectRepositoryTest {

    @Autowired
    LiquibaseRunner liquibase;

    @Autowired
    BaseCatalogRepository baseCatalogRepository;

    @Autowired
    ProjectRepository repository;

    @BeforeEach
    void setup() {
        log.debug("setup started");

        liquibase.dropAll();
        liquibase.runChangelog("db-tailoringexpert/tailoringexpert.changelog-root.xml");

        log.debug("setup completed");
    }

    @Test
    void save_ProjectEntityValid_ProjectSaved() throws IOException {
        // arrange

        // act
        ProjectEntity actual = repository.save(ProjectEntity.builder()
            .identifier("SAMPLE")
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
    @Transactional
    void findByIdentifier_ProjectExists_ProjectReturned() throws IOException {
        // arrange
        repository.save(ProjectEntity.builder().identifier("SAMPLE").build());
        repository.save(ProjectEntity.builder().identifier("SAMPLE2").build());

        // act
        ProjectEntity actual = repository.findByIdentifier("SAMPLE2");

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getIdentifier()).isEqualTo("SAMPLE2");
    }

    @Test
    @Transactional
    void findTailoringe_2TailoringExists_RequestedTailoringReturned() throws IOException {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
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
    @Transactional
    void deleteByIdentifier_ProjectExists_ProjectDeleted() throws IOException {
        // arrange
        repository.save(ProjectEntity.builder().identifier("SAMPLE").build());
        repository.save(ProjectEntity.builder().identifier("SAMPLE2").build());

        // act
        Long actual = repository.deleteByIdentifier("SAMPLE");

        // assert
        assertThat(actual)
            .isNotNull()
            .isEqualTo(1);
        assertThat(repository.findByIdentifier("SAMPLE")).isNull();
    }
}
