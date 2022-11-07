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
 * Service for handling (peristent) data used by @see {@link RequirementService}.
 *
 * @author Michael Bädorf
 */
public interface RequirementServiceRepository {

    /**
     * Load requirement defined with chapter and position.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param chapter   chapter containing requirement
     * @param position  postion of requirement in chapter
     * @return loaded requirement
     */
    Optional<TailoringRequirement> getRequirement(String project, String tailoring, String chapter, String position);

    /**
     * Load chapter of tailoring catalog.
     *
     * @param project   project identifer
     * @param tailoring tailoring name
     * @param chapter   chapter to load
     * @return loaded chapter
     */
    Optional<Chapter<TailoringRequirement>> getChapter(String project, String tailoring, String chapter);

    /**
     * Update requirement.
     *
     * @param project     project identifier
     * @param tailoring   tailoring name
     * @param chapter     chapter, requirement is part of
     * @param requirement requirement to update
     * @return updated requirement
     */
    Optional<TailoringRequirement> updateRequirement(String project, String tailoring, String chapter, TailoringRequirement requirement);

    /**
     * Update all direct and indirect requirements state of chapter.
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param chapter   chapter, requirements shall be updated
     * @return updated chapter
     */
    Optional<Chapter<TailoringRequirement>> updateSelected(String project, String tailoring, Chapter<TailoringRequirement> chapter);

    /**
     * Update chapter.<p>
     * New requirements will be created in corresponding chapter
     *
     * @param project   project identifier
     * @param tailoring tailoring name
     * @param chapter   chapter to update with new requirement(s)
     * @return updated chapter
     */
    Optional<Chapter<TailoringRequirement>> updateChapter(String project, String tailoring, Chapter<TailoringRequirement> chapter);
}
