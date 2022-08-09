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

import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Kapitel;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.TailoringAnforderung;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Funktion für die Ermittlung aller relevanten DRDs eines Tailorings.
 * Relevant sind die DRDs, die in Anforderungen referenziert und deren Lieferzeitpunkten
 * innerhalb der Projektphasen sind.
 */
@RequiredArgsConstructor
public class DRDProvider implements BiFunction<Kapitel<TailoringAnforderung>, Collection<Phase>, Map<DRD, Set<String>>> {

    @NonNull
    private BiPredicate<String, Collection<Phase>> predicate;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<DRD, Set<String>> apply(Kapitel<TailoringAnforderung> gruppe, Collection<Phase> phasen) {
        Map<DRD, Set<String>> result = new ConcurrentHashMap<>();
        gruppe.allKapitel()
            .forEach(subgruppe -> subgruppe.getAnforderungen()
                .stream()
                .filter(anforderung -> nonNull(anforderung.getAusgewaehlt()) && anforderung.getAusgewaehlt().booleanValue()
                    && nonNull(anforderung.getDrds()) && !anforderung.getDrds().isEmpty())
                .forEach(anforderung -> anforderung.getDrds()
                    .forEach(drd -> {
                        if (predicate.test(drd.getLieferzeitpunkt(), phasen)) {
                            Set<String> kapitel = result.get(drd);
                            if (isNull(kapitel)) {
                                kapitel = new LinkedHashSet<>();
                                result.put(drd, kapitel);
                            }
                            kapitel.add(subgruppe.getNummer());
                        }
                    }))
            );
        result.values()
            .stream()
            .forEach(a -> a.stream().sorted());
        return result;
    }

}
