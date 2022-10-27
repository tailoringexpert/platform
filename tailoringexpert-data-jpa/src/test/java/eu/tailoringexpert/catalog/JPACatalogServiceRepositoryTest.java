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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.repository.DRDRepository;
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

class JPACatalogServiceRepositoryTest {

    JPACatalogServiceRepositoryMapper mapperMock;
    BaseCatalogRepository baseCatalogRepositoryMock;
    DRDRepository drdRepositoryMock;
    JPACatalogServiceRepository repository;

    @BeforeEach
    void setup() {
        this.baseCatalogRepositoryMock = mock(BaseCatalogRepository.class);
        this.drdRepositoryMock = mock(DRDRepository.class);
        this.mapperMock = mock(JPACatalogServiceRepositoryMapper.class);
        this.repository = new JPACatalogServiceRepository(
            this.mapperMock,
            this.baseCatalogRepositoryMock,
            this.drdRepositoryMock
        );
    }

    @Test
    void createCatalog_BaseCatalogEntityNull_EmptyReturned() {
        // arrange
        given(baseCatalogRepositoryMock.setValidUntilForEmptyValidUntil(any())).willReturn(0);

        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder().build())
            .build();
        BaseCatalogEntity toSave = BaseCatalogEntity.builder().build();
        given(mapperMock.createCatalog(catalog)).willReturn(toSave);

        BaseCatalogEntity savedKatalog = null;
        given(baseCatalogRepositoryMock.save(toSave)).willReturn(savedKatalog);

        Catalog<BaseRequirement> savedMappedCatalog = null;
        given(mapperMock.createCatalog(savedKatalog)).willReturn(savedMappedCatalog);

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.createCatalog(catalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isEmpty();
        verify(mapperMock, times(1)).createCatalog(catalog);
        verify(mapperMock, times(1)).createCatalog(savedKatalog);
        verify(baseCatalogRepositoryMock, times(1)).save(toSave);
    }

    @Test
    void createCatalog_BaseCatalogEntityNoNull_OptionalNotNullWirdZurueckGegeben() {
        // arrange
        given(baseCatalogRepositoryMock.setValidUntilForEmptyValidUntil(any())).willReturn(0);

        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder().build())
            .build();
        BaseCatalogEntity toSave = BaseCatalogEntity.builder().build();
        given(mapperMock.createCatalog(catalog)).willReturn(toSave);

        BaseCatalogEntity savedKatalog = null;
        given(baseCatalogRepositoryMock.save(toSave)).willReturn(savedKatalog);

        Catalog<BaseRequirement> savedMappedCatalog = Catalog.<BaseRequirement>builder().build();
        given(mapperMock.createCatalog(savedKatalog)).willReturn(savedMappedCatalog);

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.createCatalog(catalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1)).createCatalog(catalog);
        verify(mapperMock, times(1)).createCatalog(savedKatalog);
        verify(baseCatalogRepositoryMock, times(1)).save(toSave);
    }

    @Test
    void createCatalog_CatalogNull_OptionalNullReturnd() {
        // arrange
        Catalog<BaseRequirement> catalog = null;

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.createCatalog(catalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isEmpty();
        verify(baseCatalogRepositoryMock, times(0)).save(any());
        verify(mapperMock, times(0)).createCatalog(any(Catalog.class));
        verify(mapperMock, times(0)).createCatalog(any(BaseCatalogEntity.class));
        verify(baseCatalogRepositoryMock, times(0)).save(any());
    }

    @Test
    void createCatalog_CatalogExistingDRD_ExistingDRDNotCreated() {
        // arrange
        given(baseCatalogRepositoryMock.setValidUntilForEmptyValidUntil(any())).willReturn(0);


        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder()
                .requirements(asList(
                    BaseRequirement.builder()
                        .drds(asList(
                            DRD.builder().number("04.11").build()
                        ))
                        .build())
                )
                .build())
            .build();
        BaseCatalogEntity toSave = BaseCatalogEntity.builder().build();

        given(drdRepositoryMock.findByNumber("04.11")).willReturn(DRDEntity.builder().build());

        given(mapperMock.createCatalog(catalog)).willReturn(toSave);

        BaseCatalogEntity savedKatalog = null;
        given(baseCatalogRepositoryMock.save(toSave)).willReturn(savedKatalog);

        Catalog<BaseRequirement> savedMappedCatalog = null;
        given(mapperMock.createCatalog(savedKatalog)).willReturn(savedMappedCatalog);

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.createCatalog(catalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isEmpty();
        verify(drdRepositoryMock, times(0)).save(any());
        verify(mapperMock, times(1)).createCatalog(catalog);
        verify(mapperMock, times(1)).createCatalog(savedKatalog);
        verify(baseCatalogRepositoryMock, times(1)).save(toSave);
    }

    @Test
    void createCatalog_CatalogNewDRD_NewDRDCreated() {
        // arrange
        given(baseCatalogRepositoryMock.setValidUntilForEmptyValidUntil(any())).willReturn(0);

        DRD drd = DRD.builder()
            .action("R")
            .number("04.11")
            .title("Common Cause Analysis")
            .deliveryDate("CDR; QR, AR")
            .build();
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder()
                .requirements(asList(
                    BaseRequirement.builder()
                        .drds(asList(
                            drd
                        ))
                        .build())
                )
                .build())
            .build();

        given(drdRepositoryMock.findByNumber("04.11")).willReturn(null);

        BaseCatalogEntity toSave = BaseCatalogEntity.builder().build();
        given(mapperMock.createCatalog(catalog)).willReturn(toSave);

        BaseCatalogEntity savedKatalog = null;
        given(baseCatalogRepositoryMock.save(toSave)).willReturn(savedKatalog);

        Catalog<BaseRequirement> savedMappedCatalog = null;
        given(mapperMock.createCatalog(savedKatalog)).willReturn(savedMappedCatalog);

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.createCatalog(catalog, ZonedDateTime.now());

        // assert
        assertThat(actual).isEmpty();
        verify(drdRepositoryMock, times(1)).save(any());
        verify(mapperMock, times(1)).createCatalog(catalog);
        verify(mapperMock, times(1)).createCatalog(savedKatalog);
        verify(baseCatalogRepositoryMock, times(1)).save(toSave);
    }

    @Test
    void getCatalog_NonExistingVersion_EmptyReturned() {
        // arrange
        given(baseCatalogRepositoryMock.findByVersion("4711")).willReturn(null);

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.getCatalog("4711");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getCatalog_ExisitingVersion_OptionalReturned() {
        // arrange
        BaseCatalogEntity baseCatalogEntity = BaseCatalogEntity.builder().build();
        given(baseCatalogRepositoryMock.findByVersion("4711")).willReturn(baseCatalogEntity);

        given(mapperMock.getCatalog(baseCatalogEntity)).willReturn(Catalog.<BaseRequirement>builder().build());

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.getCatalog("4711");

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1)).getCatalog(baseCatalogEntity);
    }

    @Test
    void existsCatalog_CatalogNotExists_FalseReturned() {
        // arrange
        given(baseCatalogRepositoryMock.existsByVersion("8.2.1")).willReturn(false);

        // act
        boolean actual = repository.existsCatalog("8.2.1");

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void existsCatalog_CatalogExists_TrueReturned() {
        // arrange
        given(baseCatalogRepositoryMock.existsByVersion("8.2.1")).willReturn(true);

        // act
        boolean actual = repository.existsCatalog("8.2.1");

        // assert
        assertThat(actual).isTrue();
    }
}
