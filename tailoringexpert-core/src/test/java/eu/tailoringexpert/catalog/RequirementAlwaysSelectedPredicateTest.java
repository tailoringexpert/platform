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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Requirement;
import eu.tailoringexpert.domain.TailoringRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequirementAlwaysSelectedPredicateTest {
    RequirementAlwaysSelectedPredicate predicate;

    @BeforeEach
    void setup() {
        this.predicate = new RequirementAlwaysSelectedPredicate();
    }

    @Test
    void test_requirementSelected_trueReturned() {
        // arrange
        Requirement requirement = BaseRequirement.builder().build();

        // act
        boolean actual = predicate.test(requirement);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_requiremenTSelected_falseReturned() {
        // arrange
        Requirement requirement = TailoringRequirement.builder().selected(false).build();

        // act
        boolean actual = predicate.test(requirement);

        // assert
        assertThat(actual).isTrue();
    }
}
