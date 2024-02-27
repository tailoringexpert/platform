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

import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class ReferenceParserTest {

    ToReferenceFunction parser;

    @BeforeEach
    void setup() {
        this.parser = new ToReferenceFunction();
    }

    @Test
    void apply_NonModifiedReferenceNoLogo_ReferenceWithNullLogoReturned() {
        // arrange
        String reference = "Q-ST-10C Rev.1. para. 4.1 ";
        Logo logo = null;

        // act
        Reference actual = parser.apply(reference, logo);

        //assert
        assertThat(actual).isNotNull();
        assertThat(actual.getText()).isEqualTo("Q-ST-10C Rev.1. para. 4.1");
        assertThat(actual.getChanged()).isFalse();
        assertThat(actual.getLogo()).isNull();
    }

    @Test
    void apply_ModifiedReferenceNoLogo_ReferenceWithNullLogoReturned() {
        // arrange
        String reference = "Q-ST-10C Rev.1. para. 4.1    (mod)";
        Logo logo = null;

        // act
        Reference actual = parser.apply(reference, logo);

        //assert
        assertThat(actual).isNotNull();
        assertThat(actual.getText()).isEqualTo("Q-ST-10C Rev.1. para. 4.1");
        assertThat(actual.getChanged()).isTrue();
        assertThat(actual.getLogo()).isNull();
    }

}
