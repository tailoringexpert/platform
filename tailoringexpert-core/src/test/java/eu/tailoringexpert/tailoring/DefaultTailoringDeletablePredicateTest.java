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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DefaultTailoringDeletablePredicateTest {

    TailoringDeletablePredicateRepository repository;

    DefaultTailoringDeletablePredicate predicate;

    @BeforeEach
    void beforeEach() {
        this.repository = mock(TailoringDeletablePredicateRepository.class);
        this.predicate = new DefaultTailoringDeletablePredicate(this.repository);
    }

    @Test
    void test_StateCreated_TrueReturned() {
        // arrange
        given(repository.getTailoringState("SAMPLE", "master"))
            .willReturn(Optional.of(TailoringState.CREATED));

        // act
        Boolean actual = predicate.test("SAMPLE", "master");


        // assert
        assertThat(actual).isTrue();
        verify(repository, times(1)).getTailoringState("SAMPLE", "master");
    }

    @Test
    void test_StateAgreed_FalseReturned() {
        // arrange
        given(repository.getTailoringState("SAMPLE", "master"))
            .willReturn(Optional.of(TailoringState.AGREED));

        // act
        Boolean actual = predicate.test("SAMPLE", "master");


        // assert
        assertThat(actual).isFalse();
        verify(repository, times(1)).getTailoringState("SAMPLE", "master");
    }

    @Test
    void test_StateReleased_FalseReturned() {
        // arrange
        given(repository.getTailoringState("SAMPLE", "master"))
            .willReturn(Optional.of(TailoringState.RELEASED));

        // act
        Boolean actual = predicate.test("SAMPLE", "master");


        // assert
        assertThat(actual).isFalse();
        verify(repository, times(1)).getTailoringState("SAMPLE", "master");
    }
}
