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

import eu.tailoringexpert.domain.Phase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.Phase.D;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.assertj.core.api.Assertions.assertThat;

class DRDApplicablePredicateTest {

    private DRDApplicablePredicate predicate;

    @BeforeEach
    void setup() {
        this.predicate = new DRDApplicablePredicate(Map.ofEntries(
            new SimpleEntry<Phase, Collection<String>>(ZERO, unmodifiableCollection(asList("MDR"))),
            new SimpleEntry<Phase, Collection<String>>(A, unmodifiableCollection(asList("SRR"))),
            new SimpleEntry<Phase, Collection<String>>(B, unmodifiableCollection(asList("PDR"))),
            new SimpleEntry<Phase, Collection<String>>(C, unmodifiableCollection(asList("CDR"))),
            new SimpleEntry<Phase, Collection<String>>(D, unmodifiableCollection(asList("AR", "DRB", "FRR", "LRR"))),
            new SimpleEntry<Phase, Collection<String>>(E, unmodifiableCollection(asList("ORR"))),
            new SimpleEntry<Phase, Collection<String>>(F, unmodifiableCollection(asList("EOM"))))
        );
    }

    @Test
    void test_MilestoneNotInRelevantPhase_FalseReturned() {
        // arrange
        String deliveryDate = "PDR";
        Collection<Phase> phases = asList(E);

        // act
        boolean actual = predicate.test(deliveryDate, phases);

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void test_MilestoneInRelevantPhase_TrueReturned() {
        // arrange
        String deliveryDate = "ORR";
        Collection<Phase> phases = asList(E);

        // act
        boolean actual = predicate.test(deliveryDate, phases);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_CustomTextMilestoneNotInRelevantPhase_TrueReturned() {
        // arrange
        String deliveryDate = "PDR;on regular basis within project progress";
        Collection<Phase> phases = asList(E);

        // act
        boolean actual = predicate.test(deliveryDate, phases);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_MultipleMilestonesOneRelevant_TrueReturned() {
        // arrange
        String deliveryDate = "ORR;on regular basis within project progress";
        Collection<Phase> phases = asList(D, E);

        // act
        boolean actual = predicate.test(deliveryDate, phases);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_MultipleMilestonesNonRelevant_FalseReturned() {
        // arrange
        String deliveryDate = "PDR";
        Collection<Phase> phases = asList(D, E);

        // act
        boolean actual = predicate.test(deliveryDate, phases);

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void test_MultipleMilestonesNonRelevantMilestoneCustomText_TrueReturned() {
        // arrange
        String deliveryDate = "PDR;on regular basis within project progress";
        Collection<Phase> phases = asList(D, E);

        // act
        boolean actual = predicate.test(deliveryDate, phases);

        // assert
        assertThat(actual).isTrue();
    }
}
