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

import static eu.tailoringexpert.domain.ProjectState.ONGOING;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringJUnitConfig(classes = {DBConfiguration.class})
@Transactional
class TailoringIdentifierProviderRepositoryTest {

    @Autowired
    BaseCatalogRepository baseCatalogRepository;

    @Autowired
    ProjectRepository repository;

    @Autowired
    TailoringIdentifierProviderRepository provider;


    @Test
    void findTailoringIdentifier_TailoringExists_StateReturned() throws IOException {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .state(ONGOING)
            .tailorings(Arrays.asList(
                TailoringEntity.builder()
                    .name("master")
                    .identifier("1000")
                    .state(TailoringState.CREATED)
                    .build(),
                TailoringEntity.builder()
                    .name("master1")
                    .identifier("1001")
                    .state(TailoringState.AGREED)
                    .build()
            ))
            .build();
        repository.save(project);

        // act
        String actual = provider.apply("SAMPLE", "master1");

        // assert
        assertThat(actual).isEqualTo("1001");
    }

    @Test
    void findTailoringIdentifier_TailoringNotExists_NullReturned() throws IOException {
        // arrange
        ProjectEntity project = ProjectEntity.builder()
            .identifier("SAMPLE")
            .state(ONGOING)
            .tailorings(Arrays.asList(
                TailoringEntity.builder()
                    .name("master")
                    .identifier("1000")
                    .state(TailoringState.CREATED)
                    .build(),
                TailoringEntity.builder()
                    .name("master1")
                    .identifier("1001")
                    .state(TailoringState.AGREED)
                    .build()
            ))
            .build();
        repository.save(project);

        // act
        String actual = provider.apply("SAMPLE_1", "master1");

        // assert
        assertThat(actual).isNull();
    }
}
