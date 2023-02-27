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

import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.SelectionVectorProfileEntity;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPADocumentServiceRepositoryTest {

    JPADocumentServiceRepositoryMapper mapperMock;
    SelectionVectorProfileRepository selectionVectorProfileRepositoryMock;
    JPADocumentServiceRepository repository;

    @BeforeEach
    void setup() {
        this.selectionVectorProfileRepositoryMock = mock(SelectionVectorProfileRepository.class);
        this.mapperMock = mock(JPADocumentServiceRepositoryMapper.class);
        this.repository = new JPADocumentServiceRepository(
            this.mapperMock,
            this.selectionVectorProfileRepositoryMock
        );
    }


    @Test
    void getSelectionVectorProfiles_ProfilesNotExists_EmptyCollectionReturned() {
        // arrange
        given(selectionVectorProfileRepositoryMock.findAll()).willReturn(Collections.emptyList());
        given(mapperMock.getSelectionVectorProfiles(any())).willReturn(SelectionVectorProfile.builder().build());

        // act
        Collection<SelectionVectorProfile> actual = repository.getSelectionVectorProfiles();

        // assert
        assertThat(actual)
            .isNotNull()
            .isEmpty();
        verify(mapperMock, times(0)).getSelectionVectorProfiles(any());
    }

    @Test
    void getSelectionVectorProfiles_ProfilesExists_NonEmptyCollectionReturned() {
        // arrange
        given(selectionVectorProfileRepositoryMock.findAll()).willReturn(List.of(
            SelectionVectorProfileEntity.builder().build(),
            SelectionVectorProfileEntity.builder().build()
        ));
        given(mapperMock.getSelectionVectorProfiles(any())).willReturn(SelectionVectorProfile.builder().build());

        // act
        Collection<SelectionVectorProfile> actual = repository.getSelectionVectorProfiles();

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(2);
        verify(mapperMock, times(2)).getSelectionVectorProfiles(any());
    }
}
