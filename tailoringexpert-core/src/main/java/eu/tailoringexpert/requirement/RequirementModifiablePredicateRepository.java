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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringState;

import java.util.Optional;

/**
 * Service for handling (peristent) data used by @see {@link RequirementModifiablePredicate}.
 *
 * @author Michael Bädorf
 */
public interface RequirementModifiablePredicateRepository {

    /**
     * Retrieves the state of requested tailoring.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @return state of tailoring if exists, otherwise empty
     */
    Optional<TailoringState> getTailoringState(String project, String tailoring);
}
