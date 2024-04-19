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

import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseCatalogVersionProjection;
import eu.tailoringexpert.domain.BaseRequirementEntity;
import eu.tailoringexpert.domain.IdentifierEntity;
import eu.tailoringexpert.domain.BaseCatalogChapterEntity;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.stream.Stream;

import static eu.tailoringexpert.domain.Phase.A;
import static java.util.Arrays.asList;
import static java.util.Set.of;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringJUnitConfig({DBConfiguration.class})
@Transactional
class BaseCatalogRepositoryTest {

    @Autowired
    BaseCatalogRepository repository;

    @Test
    void save_ValidBaseCatalog_BaseCatalogSaved() {
        // arrange
        BaseRequirementEntity requirement = BaseRequirementEntity.builder()
            .phase(A)
            .text("First requirement")
            .identifiers(Stream.of(
                    IdentifierEntity.builder()
                        .type("W")
                        .limitations(of("SAT"))
                        .level(4)
                        .build()
                ).collect(toSet())
            )
            .build();

        BaseCatalogChapterEntity toc = BaseCatalogChapterEntity.builder()
            .requirements(asList(requirement))
            .name("Chapter 1")
            .position(1)
            .build();

        BaseCatalogEntity entity = BaseCatalogEntity.builder()
            .version("7.2")
            .toc(toc)
            .build();

        // act
        BaseCatalogEntity actual = repository.save(entity);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    void findByVersion_BaseCatalogExists_BaseCatalogReturned() {
        // arrange
        repository.save(BaseCatalogEntity.builder()
            .version("7.2.1")
            .build());
        repository.save(BaseCatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        BaseCatalogEntity actual = repository.findByVersion("8.2.1");

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
    }

    @Test
    void findCatalogVersionBy_2BaseCatalogExists_ListWith2BaseCatalogsReturned() {
        // arrange
        repository.save(BaseCatalogEntity.builder()
            .version("7.2.1")
            .build());
        repository.save(BaseCatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        Collection<BaseCatalogVersionProjection> actual = repository.findCatalogVersionBy();

        // assert
        log.debug(actual);
        assertThat(actual)
            .isNotNull()
            .hasSize(2);
    }

    @Test
    void existsByVersion_CatalogNotExists_FalseReturned() {
        // arrange
        repository.save(BaseCatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        boolean actual = repository.existsByVersion("9");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void existsByVersion_CatalogExists_TrueReturned() {
        // arrange
        repository.save(BaseCatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        boolean actual = repository.existsByVersion("8.2.1");

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void findCatalogByVersion_VersionNotExist_NullReturned() {
        // arrange
        repository.save(BaseCatalogEntity.builder()
            .version("7.2.1")
            .build());

        repository.save(BaseCatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        BaseCatalogVersionProjection actual = repository.findCatalogByVersion("9.2.1");

        // assert
        assertThat(actual)
            .isNull();
    }

    @Test
    void findCatalogByVersion_2BaseCatalogExistsVersionExist_CorrectVersionReturned() {
        // arrange
        repository.save(BaseCatalogEntity.builder()
            .version("7.2.1")
            .build());

        repository.save(BaseCatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        BaseCatalogVersionProjection actual = repository.findCatalogByVersion("8.2.1");

        // assert
        assertThat(actual)
            .isNotNull();
    }

    @Test
    void setValidUntilForVersion_VersionNonExist_0Returned() {
        // arrange
        ZonedDateTime now = ZonedDateTime.now();

        // act
        int actual = repository.setValidUntilForVersion("8.2.1", now);

        // assert
        assertThat(actual).isZero();

    }

    @Test
    void setValidUntilForVersion_VersionExist_1Returned() {
        // arrange
        ZonedDateTime now = ZonedDateTime.now();

        repository.save(BaseCatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        int actual = repository.setValidUntilForVersion("8.2.1", now);

        // assert
        assertThat(actual).isOne();
    }

}

