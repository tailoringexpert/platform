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

import eu.tailoringexpert.domain.TailoringState;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

/**
 * Predicate to check if a tailoring can be deleted.<p>
 * A tailoring can be deleted only in state {@code TailoringState.CREATED}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class DefaultTailoringDeletablePredicate implements TailoringDeletablePredicate {

    @NonNull
    private TailoringDeletablePredicateRepository repository;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(String project, String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);
        Optional<TailoringState> state = repository.getTailoringState(project, tailoring);
        return log.traceExit(TailoringState.CREATED.compareTo(state.orElse(TailoringState.RELEASED)) == 0);
    }
}
