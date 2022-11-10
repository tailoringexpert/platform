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
package eu.tailoringexpert.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TailoringStateTest {

    @Test
    void nextState_CREATED_AGREEDReturned() {
        // arrange
        TailoringState state = TailoringState.CREATED;

        // act
        TailoringState actual = state.nextState();

        // assert
        assertThat(actual).isEqualTo(TailoringState.AGREED);
    }

    @Test
    void nextState_AGREED_RELEASEDReturned() {
        // arrange
        TailoringState state = TailoringState.AGREED;

        // act
        TailoringState actual = state.nextState();

        // assert
        assertThat(actual).isEqualTo(TailoringState.RELEASED);
    }

    @Test
    void nextState_RELEASED_RELEASEDReturned() {
        // arrange
        TailoringState state = TailoringState.RELEASED;

        // act
        TailoringState actual = state.nextState();

        // assert
        assertThat(actual).isEqualTo(TailoringState.RELEASED);
    }

    @Test
    void isBefore_CREATEDComparedToCREATED_FalseReturned() {
        // arrange
        TailoringState state = TailoringState.CREATED;

        // act
        boolean actual = state.isBefore(TailoringState.CREATED);

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void isBefore_CREATEDComparedToAGREED_TrueReturned() {
        // arrange
        TailoringState state = TailoringState.CREATED;

        // act
        boolean actual = state.isBefore(TailoringState.AGREED);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void isBefore_AGRREEDComparedToCREATED_FalseReturned() {
        // arrange
        TailoringState state = TailoringState.AGREED;

        // act
        boolean actual = state.isBefore(TailoringState.CREATED);

        // assert
        assertThat(actual).isFalse();
    }

}
