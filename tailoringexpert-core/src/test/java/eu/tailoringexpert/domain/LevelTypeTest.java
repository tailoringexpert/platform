/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LevelTypeTest {

    @Test
    void fromString_ValidId_EnumReturned() {
        // arrange

        // act
        LevelType actual = LevelType.fromString("+");

        // assert
        assertThat(actual).isEqualTo(LevelType.EQUAL);
    }

    @Test
    void fromString_InValidId_DefaultEnumReturned() {
        // arrange

        // act
        LevelType actual = LevelType.fromString("tdteuwvdewvdew");

        // assert
        assertThat(actual).isEqualTo(LevelType.DEFAULT);
    }
}
