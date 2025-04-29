/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2025 Michael Bädorf and others
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

import eu.tailoringexpert.domain.TailoringRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequirementSelectedPredicateTest {

    RequirementSelectedPredicate predicate;

    @BeforeEach
    void setup() {
        this.predicate = new RequirementSelectedPredicate();
    }

    @Test
    void test_requirementSelected_trueReturned() {
        // arrange
        TailoringRequirement requirement = TailoringRequirement.builder().selected(true).build();

        // act
        boolean actual = predicate.test(requirement);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_requirementNotSelected_falseReturned() {
        // arrange
        TailoringRequirement requirement = TailoringRequirement.builder().selected(false).build();

        // act
        boolean actual = predicate.test(requirement);

        // assert
        assertThat(actual).isFalse();
    }
}
