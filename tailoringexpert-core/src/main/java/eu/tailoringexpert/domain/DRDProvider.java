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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

/**
 * Function for determinating all relevant DRDs of a tailoring.<p>
 * Revelant are DRDs, which are referenced in requirements and their delivery date are within the phases.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class DRDProvider<T extends Requirement> implements BiFunction<Chapter<T>, Collection<Phase>, Map<DRD, Set<String>>> {

    @NonNull
    private Predicate<T> selectionPredicate;

    @NonNull
    private BiPredicate<String, Collection<Phase>> predicate;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<DRD, Set<String>> apply(Chapter<T> chapter, Collection<Phase> phases) {
        log.traceEntry(chapter::getNumber, () -> phases);

        Map<DRD, Set<String>> result = new ConcurrentHashMap<>();
        chapter.allChapters()
            .forEach(subChapter ->
                ofNullable(subChapter.getRequirements())
                    .ifPresent(requirements -> requirements.stream()
                        .filter(selectionPredicate)
                        .filter(Requirement::hasDRD)
                        .forEach(requirement -> requirement.getDrds()
                            .forEach(drd -> {
                                if (predicate.test(drd.getDeliveryDate(), phases)) {
                                    Set<String> chapters = result.get(drd);
                                    if (isNull(chapters)) {
                                        chapters = new LinkedHashSet<>();
                                        result.put(drd, chapters);
                                    }
                                    chapters.add(subChapter.getNumber());
                                }
                            })
                        )
                    )
            );

        result.values()
            .forEach(a -> a.stream().sorted());

        return log.traceExit(result);
    }

}
