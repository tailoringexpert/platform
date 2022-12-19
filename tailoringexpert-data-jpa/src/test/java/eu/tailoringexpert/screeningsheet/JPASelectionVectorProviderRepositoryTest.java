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
package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.domain.SelectionVectorProfileEntity;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class JPASelectionVectorProviderRepositoryTest {
    JPASelectionVectorProviderRepositoryMapper mapperMock;
    SelectionVectorProfileRepository selectionVectorProfileRepositoryMock;
    JPASelectionVectorProviderRepository repository;

    @BeforeEach
    void setup() {
        this.mapperMock = mock(JPASelectionVectorProviderRepositoryMapper.class);
        this.selectionVectorProfileRepositoryMock = mock(SelectionVectorProfileRepository.class);
        this.repository = new JPASelectionVectorProviderRepository(
            mapperMock,
            selectionVectorProfileRepositoryMock
        );
    }

    @Test
    void getSelectionVectorProfile_ProfileNotExists_EmptyReturned() {
        // arrange
        String profile = "I_DONT_KNOW";
        given(selectionVectorProfileRepositoryMock.findByInternalKey(profile)).willReturn(null);

        // act
        Optional<SelectionVectorProfile> actual = repository.getSelectionVectorProfile(profile);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getSelectionVectorProfile_ProfileExists_ProfileReturned() {
        // arrange
        String profile = "PROFIL01";
        given(selectionVectorProfileRepositoryMock.findByInternalKey(profile))
            .willReturn(SelectionVectorProfileEntity.builder().build());

        given(mapperMock.toDomain(any(SelectionVectorProfileEntity.class)))
            .willReturn(SelectionVectorProfile.builder().build());

        // act
        Optional<SelectionVectorProfile> actual = repository.getSelectionVectorProfile(profile);

        // assert
        assertThat(actual).isPresent();
    }

}
