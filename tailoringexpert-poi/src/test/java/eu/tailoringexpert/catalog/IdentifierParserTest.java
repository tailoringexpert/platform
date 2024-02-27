/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2024 Michael Bädorf and others
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

import eu.tailoringexpert.domain.Identifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class IdentifierParserTest {

    ToIdentifierFunction parser;

    @BeforeEach
    void setup() {
        this.parser = new ToIdentifierFunction();
    }

    @Test
    void apply_IdentifierStringWithNoLimitations_IdentifierReturned() {
        // arrange
        String identifier = "Q5 ";

        // act
        Identifier actual = parser.apply(identifier);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getType()).isEqualTo("Q");
        assertThat(actual.getLevel()).isEqualTo(5);
        assertThat(actual.getLimitations()).isNull();
    }

    @Test
    void apply_IdentifierStringWithSpacesNoLimitations_IdentifierReturned() {
        // arrange
        String identifier = " Q 5  ";

        // act
        Identifier actual = parser.apply(identifier);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getType()).isEqualTo("Q");
        assertThat(actual.getLevel()).isEqualTo(5);
        assertThat(actual.getLimitations()).isNull();
    }
    @Test
    void apply_IdentifierStringWith2Limitations_IdentifierReturned() {
        // arrange
        String identifier = "Q5 (ISS)(ESA)";

        // act
        Identifier actual = parser.apply(identifier);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getType()).isEqualTo("Q");
        assertThat(actual.getLevel()).isEqualTo(5);
        assertThat(actual.getLimitations())
            .hasSize(2)
            .containsExactly("ISS", "ESA");
    }
}
