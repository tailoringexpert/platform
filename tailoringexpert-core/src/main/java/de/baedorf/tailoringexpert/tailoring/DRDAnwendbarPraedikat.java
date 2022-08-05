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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Prädikat, ob der Lieferzeitpunkt in den (übergebenen) Phasen enthalten ist.
 */
public class DRDAnwendbarPraedikat implements BiPredicate<String, Collection<Phase>> {

    private Map<Phase, Collection<String>> phase2Meilensteine;
    private Collection<String> meilensteine;

    public DRDAnwendbarPraedikat(Map<Phase, Collection<String>> phase2Meilensteine) {
        this.phase2Meilensteine = phase2Meilensteine;
        this.meilensteine = this.phase2Meilensteine.values().stream()
            .flatMap(Collection::stream)
            .collect(toUnmodifiableSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(String lieferzeitpunkt, Collection<Phase> phasen) {
        // alle meilensteine phasensteine der phase ermitteln
        List<String> dueDates = Collections.list(new StringTokenizer(lieferzeitpunkt, ";")).stream()
            .map(token -> token.toString().trim())
            .collect(Collectors.toList());

        // für jede phase prüfen
        Optional<Phase> result = phasen.stream()
            .filter(phase -> {
                Collection<String> phasenMeilensteine = phase2Meilensteine.get(phase);
                return dueDates.stream()
                    .anyMatch(dueDate -> phasenMeilensteine.contains(dueDate) || !meilensteine.contains(dueDate));
            })
            .findFirst();

        return result.isPresent();
    }

}
