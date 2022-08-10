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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static java.util.Arrays.asList;

class ProjektEntityTest {

    @Test
    void getProjektPhase_PhaseNull_EmptyWirdZurueckgegen() {
        // arrange
        ProjektEntity entity = ProjektEntity.builder()
            .tailorings(Collections.emptyList()
            )
            .build();

        // act
        Optional<TailoringEntity> actual = entity.getTailoring(null);

        // assert
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void getProjektPhase_ProjektOhnePhasen_EmptyWirdZurueckgegen() {
        // arrange
        ProjektEntity entity = ProjektEntity.builder()
            .tailorings(Collections.emptyList()
            )
            .build();

        // act
        Optional<TailoringEntity> actual = entity.getTailoring("master1");

        // assert
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void getProjektPhase_ProjektMitPhasenPhaseNichtVorhanden_EmptyWirdZurueckgegen() {
        // arrange
        ProjektEntity entity = ProjektEntity.builder()
            .tailorings(asList(
                TailoringEntity.builder().name("master").build()
            ))
            .build();

        // act
        Optional<TailoringEntity> actual = entity.getTailoring("master1");

        // assert
        Assertions.assertThat(actual).isEmpty();
    }

    @Test
    void getProjektPhase_PhasenPhaseVorhanden_PhaseWirdZurueckgegen() {
        // arrange
        ProjektEntity entity = ProjektEntity.builder()
            .tailorings(asList(
                TailoringEntity.builder().name("master").build()
            ))
            .build();

        // act
        Optional<TailoringEntity> actual = entity.getTailoring("master");

        // assert
        Assertions.assertThat(actual).isNotEmpty();
    }
}
