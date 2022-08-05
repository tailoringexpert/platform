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
package de.baedorf.tailoringexpert.repository;

import de.baedorf.tailoringexpert.domain.IdentifikatorEntity;
import de.baedorf.tailoringexpert.domain.KatalogAnforderungEntity;
import de.baedorf.tailoringexpert.domain.KatalogEntity;
import de.baedorf.tailoringexpert.domain.KatalogKapitelEntity;
import de.baedorf.tailoringexpert.domain.KatalogVersion;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.stream.Stream;

import static de.baedorf.tailoringexpert.domain.Phase.A;
import static java.util.Arrays.asList;
import static java.util.Set.of;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
@SpringJUnitConfig(classes = {DBConfiguration.class})
@EnableJpaRepositories
@TestPropertySource("classpath:h2.properties")
@EnableTransactionManagement
@DirtiesContext
class KatalogRepositoryTest {
    @Autowired
    LiquibaseRunner liquibase;

    @Autowired
    KatalogRepository repository;

    @BeforeEach
    void setup() {
        log.debug("setup started");

        liquibase.dropAll();
        liquibase.runChangelog("db-tailoringexpert-install.xml");

        log.debug("setup completed");
    }

    @Test
    void save_GueltigerKatalog_KatalogWirdGespeichert() {
        // arrange
        KatalogAnforderungEntity anforderung = KatalogAnforderungEntity.builder()
            .phase(A)
            .text("Die erste Anforderung")
            .identifikatoren(Stream.of(
                IdentifikatorEntity.builder()
                    .typ("W")
                    .limitierungen(of("SAT"))
                    .level(4)
                    .build()
                ).collect(toSet())
            )
            .build();

        KatalogKapitelEntity toc = KatalogKapitelEntity.builder()
            .anforderungen(asList(anforderung))
            .name("Gruppe 1")
            .position(1)
            .build();

        KatalogEntity entity = KatalogEntity.builder()
            .version("7.2")
            .toc(toc)
            .build();

        // act
        KatalogEntity actual = repository.save(entity);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
    }

    @Test
    @Transactional
    void findByVersion_KatalogVorhanden_KatalogWirdZurueckGegeben() throws IOException {
        // arrange
        repository.save(KatalogEntity.builder()
            .version("7.2.1")
            .build());
        repository.save(KatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        KatalogEntity actual = repository.findByVersion("8.2.1");

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getVersion()).isEqualTo("8.2.1");
    }

    @Test
    @Transactional
    void findKatalogVersionBy_2KatalogeVorhanden_KatalogListeMit2KatalogenWirdZurueckGegeben() throws IOException {
        // arrange
        repository.save(KatalogEntity.builder()
            .version("7.2.1")
            .build());
        repository.save(KatalogEntity.builder()
            .version("8.2.1")
            .build());

        // act
        Collection<KatalogVersion> actual = repository.findKatalogVersionBy();

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(2);
    }

    @Test
    @Transactional
    void setGueltigBisFuerNichtGesetztesGueltigBis_2GueltigeKatalogeVerhanden_2KatalogeBeendet() {
        // arrange
        repository.save(KatalogEntity.builder()
            .version("7.2.1")
            .build());
        repository.save(KatalogEntity.builder()
            .version("8.2.1")
            .build());

        ZonedDateTime gueltigBis = ZonedDateTime.of(LocalDateTime.of(2021, 12, 31, 23, 59), ZoneId.of("Europe/Berlin"));

        // act
        int actual = repository.setGueltigBisFuerNichtGesetztesGueltigBis(gueltigBis);

        // assert
        assertThat(actual).isEqualTo(2);
    }

    @Test
    @Transactional
    void setGueltigBisFuerNichtGesetztesGueltigBis_1GueltigeKatalogeVerhanden_1KatalogeBeendet() {
        // arrange
        repository.save(KatalogEntity.builder()
            .version("7.2.1")
            .gueltigBis(ZonedDateTime.of(LocalDateTime.of(2020, 12, 31, 23, 59), ZoneId.of("Europe/Berlin")))
            .build());
        repository.save(KatalogEntity.builder()
            .version("8.2.1")
            .build());

        ZonedDateTime gueltigBis = ZonedDateTime.of(LocalDateTime.of(2021, 12, 31, 23, 59), ZoneId.of("Europe/Berlin"));

        // act
        int actual = repository.setGueltigBisFuerNichtGesetztesGueltigBis(gueltigBis);

        // assert
        assertThat(actual).isEqualTo(1);
    }
}
