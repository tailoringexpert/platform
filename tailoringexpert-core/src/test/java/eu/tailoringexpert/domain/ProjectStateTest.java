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

import static eu.tailoringexpert.domain.ProjectState.COMPLETED;
import static eu.tailoringexpert.domain.ProjectState.ONGOING;
import static org.assertj.core.api.Assertions.assertThat;

class ProjectStateTest {

    @Test
    void nextState_ONGOING_COMPLETEDReturned() {
        // arrange
        ProjectState state = ONGOING;

        // act
        ProjectState actual = state.nextState();

        // assert
        assertThat(actual).isEqualTo(COMPLETED);
    }

    @Test
    void nextState_COMPLETED_COMPLETEDReturned() {
        // arrange
        ProjectState state = COMPLETED;

        // act
        ProjectState actual = state.nextState();

        // assert
        assertThat(actual).isEqualTo(COMPLETED);
    }



}
