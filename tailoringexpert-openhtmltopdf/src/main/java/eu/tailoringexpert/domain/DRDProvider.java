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

import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import static java.util.Comparator.comparing;

/**
 * Function for determinating all relevant DRDs of a tailoring.<p>
 * Revelant are DRDs, which are referenced in requirements and their delivery date are within the phases.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class DRDProvider  <T extends Requirement>  implements Function<Chapter<T>, Set<DRD>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DRD> apply(Chapter<T> chapter) {
        Set<DRD> result = new TreeSet<>(comparing(DRD::getNumber));
        chapter.allChapters()
            .forEach(subChapter -> subChapter.getRequirements()
                .stream()
                .filter(Requirement::hasDRD)
                .forEach(requirement -> result.addAll(requirement.getDrds()))
            );
        return result;
    }

}
