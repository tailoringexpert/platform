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
package de.baedorf.tailoringexpert.katalog;

import de.baedorf.tailoringexpert.domain.DRDEntity;
import de.baedorf.tailoringexpert.domain.KatalogEntity;
import de.baedorf.tailoringexpert.repository.KatalogRepository;
import de.baedorf.tailoringexpert.domain.DRD;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import de.baedorf.tailoringexpert.repository.DRDRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPAKatalogServiceRepositoryTest {

    JPAKatalogServiceRepositoryMapper mapperMock;
    KatalogRepository katalogDefinitionRepositoryMock;
    DRDRepository drdRepositoryMock;
    JPAKatalogServiceRepository repository;

    @BeforeEach
    void setup() {
        this.katalogDefinitionRepositoryMock = mock(KatalogRepository.class);
        this.drdRepositoryMock = mock(DRDRepository.class);
        this.mapperMock = mock(JPAKatalogServiceRepositoryMapper.class);
        this.repository = new JPAKatalogServiceRepository(
            this.mapperMock,
            this.katalogDefinitionRepositoryMock,
            this.drdRepositoryMock
        );
    }

    @Test
    void createKatalog_KatalogUebergeben_OptionalNullWirdZurueckGegeben() {
        // arrange
        given(katalogDefinitionRepositoryMock.setGueltigBisFuerNichtGesetztesGueltigBis(any())).willReturn(0);

        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .toc(Kapitel.<KatalogAnforderung>builder().build())
            .build();
        KatalogEntity toSave = KatalogEntity.builder().build();
        given(mapperMock.createKatalog(katalog)).willReturn(toSave);

        KatalogEntity savedKatalog = null;
        given(katalogDefinitionRepositoryMock.save(toSave)).willReturn(savedKatalog);

        Katalog<KatalogAnforderung> savedMappedKatalog = null;
        given(mapperMock.createKatalog(savedKatalog)).willReturn(savedMappedKatalog);

        // act
        Optional<Katalog<KatalogAnforderung>> actual = repository.createKatalog(katalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(1)).createKatalog(katalog);
        verify(mapperMock, times(1)).createKatalog(savedKatalog);
        verify(katalogDefinitionRepositoryMock, times(1)).save(toSave);
    }

    @Test
    void createKatalog_KatalogUebergeben_OptionalNotNullWirdZurueckGegeben() {
        // arrange
        given(katalogDefinitionRepositoryMock.setGueltigBisFuerNichtGesetztesGueltigBis(any())).willReturn(0);

        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .toc(Kapitel.<KatalogAnforderung>builder().build())
            .build();
        KatalogEntity toSave = KatalogEntity.builder().build();
        given(mapperMock.createKatalog(katalog)).willReturn(toSave);

        KatalogEntity savedKatalog = null;
        given(katalogDefinitionRepositoryMock.save(toSave)).willReturn(savedKatalog);

        Katalog<KatalogAnforderung> savedMappedKatalog = Katalog.<KatalogAnforderung>builder().build();
        given(mapperMock.createKatalog(savedKatalog)).willReturn(savedMappedKatalog);

        // act
        Optional<Katalog<KatalogAnforderung>> actual = repository.createKatalog(katalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isNotNull();
        verify(mapperMock, times(1)).createKatalog(katalog);
        verify(mapperMock, times(1)).createKatalog(savedKatalog);
        verify(katalogDefinitionRepositoryMock, times(1)).save(toSave);
    }

    @Test
    void createKatalog_KatalogNichtUebergeben_OptionalNullWirdZurueckGegeben() {
        // arrange
        Katalog<KatalogAnforderung> katalog = null;

        // act
        Optional<Katalog<KatalogAnforderung>> actual = repository.createKatalog(katalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isEmpty();
        verify(katalogDefinitionRepositoryMock, times(0)).save(any());
        verify(mapperMock, times(0)).createKatalog(any(Katalog.class));
        verify(mapperMock, times(0)).createKatalog(any(KatalogEntity.class));
        verify(katalogDefinitionRepositoryMock, times(0)).save(any());
    }

    @Test
    void createKatalog_KatalogMitDRDBereitsGespeichertUebergeben_DRDWirdNichtNochmalsGespeichertOptionalWirdZurueckGegeben() {
        // arrange
        given(katalogDefinitionRepositoryMock.setGueltigBisFuerNichtGesetztesGueltigBis(any())).willReturn(0);


        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .toc(Kapitel.<KatalogAnforderung>builder()
                .anforderungen(asList(
                    KatalogAnforderung.builder()
                        .drds(asList(
                            DRD.builder().nummer("04.11").build()
                        ))
                        .build())
                )
                .build())
            .build();
        KatalogEntity toSave = KatalogEntity.builder().build();

        given(drdRepositoryMock.findByNummer("04.11")).willReturn(DRDEntity.builder().build());

        given(mapperMock.createKatalog(katalog)).willReturn(toSave);

        KatalogEntity savedKatalog = null;
        given(katalogDefinitionRepositoryMock.save(toSave)).willReturn(savedKatalog);

        Katalog<KatalogAnforderung> savedMappedKatalog = null;
        given(mapperMock.createKatalog(savedKatalog)).willReturn(savedMappedKatalog);

        // act
        Optional<Katalog<KatalogAnforderung>> actual = repository.createKatalog(katalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isEmpty();
        verify(drdRepositoryMock, times(0)).save(any());
        verify(mapperMock, times(1)).createKatalog(katalog);
        verify(mapperMock, times(1)).createKatalog(savedKatalog);
        verify(katalogDefinitionRepositoryMock, times(1)).save(toSave);
    }

    @Test
    void createKatalog_KatalogMitDRDNochNichtGespeichertUebergeben_DRDWirdGespeichertOptionalWirdZurueckGegeben() {
        // arrange
        given(katalogDefinitionRepositoryMock.setGueltigBisFuerNichtGesetztesGueltigBis(any())).willReturn(0);

        DRD drd = DRD.builder()
            .aktion("R")
            .nummer("04.11")
            .titel("Common Cause Analysis")
            .lieferzeitpunkt("CDR; QR, AR")
            .build();
        Katalog<KatalogAnforderung> katalog = Katalog.<KatalogAnforderung>builder()
            .toc(Kapitel.<KatalogAnforderung>builder()
                .anforderungen(asList(
                    KatalogAnforderung.builder()
                        .drds(asList(
                            drd
                        ))
                        .build())
                )
                .build())
            .build();

        given(drdRepositoryMock.findByNummer("04.11")).willReturn(null);

        KatalogEntity toSave = KatalogEntity.builder().build();
        given(mapperMock.createKatalog(katalog)).willReturn(toSave);

        KatalogEntity savedKatalog = null;
        given(katalogDefinitionRepositoryMock.save(toSave)).willReturn(savedKatalog);

        Katalog<KatalogAnforderung> savedMappedKatalog = null;
        given(mapperMock.createKatalog(savedKatalog)).willReturn(savedMappedKatalog);

        // act
        Optional<Katalog<KatalogAnforderung>> actual = repository.createKatalog(katalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isEmpty();
        verify(drdRepositoryMock, times(1)).save(any());
        verify(mapperMock, times(1)).createKatalog(katalog);
        verify(mapperMock, times(1)).createKatalog(savedKatalog);
        verify(katalogDefinitionRepositoryMock, times(1)).save(toSave);
    }

    @Test
    void getKatalog_UnbekannteVersion_EmptyWirdZurueckGegeben() {
        // arrange
        given(katalogDefinitionRepositoryMock.findByVersion("4711")).willReturn(null);

        // act
        Optional<Katalog<KatalogAnforderung>> actual = repository.getKatalog("4711");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getKatalog_BekannteVersion_OptionalWirdZurueckGegeben() {
        // arrange
        KatalogEntity katalogEntity = KatalogEntity.builder().build();
        given(katalogDefinitionRepositoryMock.findByVersion("4711")).willReturn(katalogEntity);

        given(mapperMock.getKatalog(katalogEntity)).willReturn(Katalog.<KatalogAnforderung>builder().build());

        // act
        Optional<Katalog<KatalogAnforderung>> actual = repository.getKatalog("4711");

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1)).getKatalog(katalogEntity);
    }
}
