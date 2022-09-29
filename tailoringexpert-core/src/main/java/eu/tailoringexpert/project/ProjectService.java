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
package eu.tailoringexpert.project;

import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;

import java.util.Optional;

/**
 * Service for management of projects.
 *
 * @author Michael Bädorf
 */
public interface ProjectService {

    /**
     * Create a new project.
     *
     * @param catalog                   Version of base catalog to use to create initial tailoring
     * @param screeningSheet            Screeningsheet to evaluate for tailoring parameters
     * @param applicableSelectionVector electionvector to use for making requirements applicable
     * @param note                      Note on created tailoring
     * @return Minimal data of created project
     */
    CreateProjectTO createProject(String catalog, byte[] screeningSheet, SelectionVector applicableSelectionVector, String note);

    /**
     * Create a new tailoring and adds to project.
     *
     * @param project                   Identifier of project to add tailoring for
     * @param catalog                   Version of base catalog to use to create tailoring
     * @param screeningSheetData        Screeningsheet to evaluate for tailoring parameters
     * @param applicableSelectionVector Selectionvector to use for making requirements applicable
     * @param note                      Note on created tailoring
     * @return added new tailoring
     */
    Optional<Tailoring> addTailoring(String project, String catalog, byte[] screeningSheetData, SelectionVector applicableSelectionVector, String note);

    /**
     * Create (full) copy of provided project.
     * Screeningsheets of tailorings will be replaced with provided data.
     *
     * @param project        Identifier of project to copy
     * @param screeningSheet ScreeningSheet of project to copy to
     * @return Created project
     */
    Optional<Project> copyProject(String project, byte[] screeningSheet);

    /**
     * Delete a project.
     *
     * @param project Name of project to delete
     * @return true, if project is deleted, false in all other cases
     */
    boolean deleteProject(String project);
}
