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

import eu.tailoringexpert.domain.ParameterEntity;
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
import java.util.Collection;

import static eu.tailoringexpert.domain.DatenType.MATRIX;
import static eu.tailoringexpert.domain.DatenType.SKALAR;
import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
@SpringJUnitConfig(classes = {DBConfiguration.class})
@EnableJpaRepositories
@TestPropertySource("classpath:h2.properties")
@EnableTransactionManagement
@DirtiesContext
class ParameterRepositoryTest {
    @Autowired
    LiquibaseRunner liquibase;

    @Autowired
    ParameterRepository repository;

    @BeforeEach
    void setup() {
        log.debug("setup started");

        liquibase.dropAll();
        liquibase.runChangelog("db-tailoringexpert-install.xml");

        log.debug("setup completed");
    }

    @Test
    @Transactional
    void findByNameIn_2ParameterInListeVorhanden_ListeMitParameternZurueckGegeben() throws IOException {
        // arrange
        repository.save(ParameterEntity.builder()
            .label("Produkttyp")
            .parameterType(MATRIX)
            .value("[[10],[10],[10],[10],[10],[10],[10],[10],[10],[10]]")
            .name("SAT")
            .category("Produkttyp")
            .build());

        repository.save(ParameterEntity.builder()
            .label("Lebensdauer")
            .parameterType(SKALAR)
            .value("3")
            .name("15 Jahre < t")
            .category("Lebensdauer")
            .build());

        repository.save(ParameterEntity.builder()
            .label("Kostenorientierung")
            .parameterType(SKALAR)
            .value("5")
            .name("150 <= k")
            .category("Kosten/Budget")
            .build());

        // act
        Collection<ParameterEntity> actual = repository.findByNameIn(Arrays.asList("SAT", "150 <= k", "4711"));

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(2);
    }
}
