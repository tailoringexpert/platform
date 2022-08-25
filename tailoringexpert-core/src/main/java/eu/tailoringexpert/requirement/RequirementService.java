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

import java.util.Optional;

/**
 * Service for handling requirements.
 *
 * @author Michael Bädorf
 */
public interface RequirementService {

    /**
     * Change selected state of a requirement defined by path.
     *
     * @param project   Identifier of the project
     * @param tailoring tailoring identifier
     * @param chapter   chapter of requirement
     * @param position  position of requirement in chapter
     * @param selected  selected state to set
     * @return update requirement
     */
    Optional<TailoringRequirement> handleSelected(String project, String tailoring, String chapter, String position, Boolean selected);

    /**
     * Change selected state of all requirement in all chapter and subchapters.
     *
     * @param project   Identifier of the project
     * @param tailoring tailoring identifier
     * @param chapter   root chapter to set selection state of requirement
     * @param selected  selection state to set
     * @return root chapter with updated requirements
     */
    Optional<Chapter<TailoringRequirement>> handleSelected(String project, String tailoring, String chapter, Boolean selected);

    /**
     * Change text of requirement.
     *
     * @param project   identifier of project
     * @param tailoring tailoring identifier
     * @param chapter   chapter requirement is member of
     * @param position  position of requirement in chapter
     * @param text      new text of requirement
     * @return modified requirement
     */
    Optional<TailoringRequirement> handleText(String project, String tailoring, String chapter, String position, String text);

    /**
     * Create a new requirement AFTER provided position.
     *
     * @param project   identifier of project
     * @param tailoring tailoring identifier
     * @param chapter   chapter to add new requirement
     * @param position  position in chapter after which requirement shall be created
     * @param text      text of new requirement
     * @return new created requirement
     */
    Optional<TailoringRequirement> createRequirement(String project, String tailoring, String chapter, String position, String text);
}
