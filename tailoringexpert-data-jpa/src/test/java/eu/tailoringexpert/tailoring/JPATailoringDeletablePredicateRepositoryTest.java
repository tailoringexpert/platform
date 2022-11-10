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

import eu.tailoringexpert.domain.TailoringState;
import eu.tailoringexpert.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class JPATailoringDeletablePredicateRepositoryTest {

    private ProjectRepository projectRepositoryMock;

    private JPATailoringDeletablePredicateRepository repository;

    @BeforeEach
    void beforeEach() {
        this.projectRepositoryMock = mock(ProjectRepository.class);
        this.repository = new JPATailoringDeletablePredicateRepository(
            projectRepositoryMock
        );
    }

    @Test
    void getTailoringState_RepositoryReturnedNull_EmptyReturned() {
        // arrange
        given(projectRepositoryMock.findTailoringState("SAMPLE", "master")).willReturn(null);

        // act
        Optional<TailoringState> actual = repository.getTailoringState("SAMPLE", "master");

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getTailoringState_RepositoryReturnedTailoring_TailoringStateReturned() {
        // arrange
        given(projectRepositoryMock.findTailoringState("SAMPLE", "master")).willReturn(TailoringState.AGREED);

        // act
        Optional<TailoringState> actual = repository.getTailoringState("SAMPLE", "master");

        // assert
        assertThat(actual)
            .isPresent()
            .contains(TailoringState.AGREED);
    }

}
