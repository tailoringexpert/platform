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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPABaseRequirementsRepositoryTest {

    BaseCatalogRepository baseCatalogRepositoryMock;
    JPABaseRequirementsProviderRepositoryMapper mapperMock;
    JPABaseRequirementsProviderRepository repository;


    @BeforeEach
    void setup() {
        this.mapperMock = mock(JPABaseRequirementsProviderRepositoryMapper.class);
        this.baseCatalogRepositoryMock = mock(BaseCatalogRepository.class);
        this.repository = new JPABaseRequirementsProviderRepository(
            mapperMock,
            baseCatalogRepositoryMock
        );
    }

    @Test
    void getCatalog_NonExistingVersion_EmptyReturned() {
        // arrange
        given(baseCatalogRepositoryMock.findByVersion("4711", BaseCatalogEntity.class)).willReturn(null);

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.getBaseCatalog("4711");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getCatalog_ExisitingVersion_OptionalReturned() {
        // arrange
        BaseCatalogEntity baseCatalogEntity = BaseCatalogEntity.builder().build();
        given(baseCatalogRepositoryMock.findByVersion("4711", BaseCatalogEntity.class)).willReturn(baseCatalogEntity);

        given(mapperMock.getBaseCatalog(baseCatalogEntity)).willReturn(Catalog.<BaseRequirement>builder().build());

        // act
        Optional<Catalog<BaseRequirement>> actual = repository.getBaseCatalog("4711");

        // assert
        assertThat(actual).isPresent();
        verify(mapperMock, times(1)).getBaseCatalog(baseCatalogEntity);
    }
}
