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
package de.baedorf.tailoringexpert.tailoring;

import de.baedorf.tailoringexpert.domain.Phase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;

import static de.baedorf.tailoringexpert.domain.Phase.A;
import static de.baedorf.tailoringexpert.domain.Phase.B;
import static de.baedorf.tailoringexpert.domain.Phase.C;
import static de.baedorf.tailoringexpert.domain.Phase.D;
import static de.baedorf.tailoringexpert.domain.Phase.E;
import static de.baedorf.tailoringexpert.domain.Phase.F;
import static de.baedorf.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.assertj.core.api.Assertions.assertThat;

class DRDAnwendbarPraedikatTest {

    private DRDAnwendbarPraedikat praedikat;

    @BeforeEach
    void setup() {
        this.praedikat = new DRDAnwendbarPraedikat(Map.ofEntries(
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
    void test_MeilensteinInPhaseNichtVorhanden_DRDNichtAnwendbar() {
        // arrange
        String lieferzeitpunkt = "PDR";
        Collection<Phase> phasen = asList(E);

        // act
        boolean actual = praedikat.test(lieferzeitpunkt, phasen);

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void test_MeilensteinInPhaseVorhanden_DRDAnwendbar() {
        // arrange
        String lieferzeitpunkt = "ORR";
        Collection<Phase> phasen = asList(E);

        // act
        boolean actual = praedikat.test(lieferzeitpunkt, phasen);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_FreitextMeilensteinInPhaseNichtVorhanden_DRDAnwendbar() {
        // arrange
        String lieferzeitpunkt = "PDR;on regular basis within project progress";
        Collection<Phase> phasen = asList(E);

        // act
        boolean actual = praedikat.test(lieferzeitpunkt, phasen);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_MehrerePhasenMeilensteinInZweiterPhase_DRDAnwendbar() {
        // arrange
        String lieferzeitpunkt = "ORR;on regular basis within project progress";
        Collection<Phase> phasen = asList(D, E);

        // act
        boolean actual = praedikat.test(lieferzeitpunkt, phasen);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_MehrerePhasenMeilensteinInKeinerPhase_DRDNichtAnwendbar() {
        // arrange
        String lieferzeitpunkt = "PDR";
        Collection<Phase> phasen = asList(D, E);

        // act
        boolean actual = praedikat.test(lieferzeitpunkt, phasen);

        // assert
        assertThat(actual).isFalse();
    }

    @Test
    void test_MehrerePhasenMeilensteinInKeinerPhaseFreitext_DRDAnwendbar() {
        // arrange
        String lieferzeitpunkt = "PDR;on regular basis within project progress";
        Collection<Phase> phasen = asList(D, E);

        // act
        boolean actual = praedikat.test(lieferzeitpunkt, phasen);

        // assert
        assertThat(actual).isTrue();
    }
}
