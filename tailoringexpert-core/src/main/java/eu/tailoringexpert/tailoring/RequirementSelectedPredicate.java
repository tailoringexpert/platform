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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.TailoringRequirement;

import java.util.function.Predicate;

/**
 * Predicate to check if a tailoring requirement is applicable/selected.
 *
 * @author Michael Baedorf
 */
public class RequirementSelectedPredicate implements Predicate<TailoringRequirement> {
    /**
     * @param tailoringRequirement requirement to check
     * @return true, if selected
     */
    @Override
    public boolean test(TailoringRequirement tailoringRequirement) {
        return tailoringRequirement.getSelected();
    }
}
