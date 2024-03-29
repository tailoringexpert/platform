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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectInformation;
import eu.tailoringexpert.domain.ProjectState;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.Tailoring;

import java.util.Collection;
import java.util.Optional;

/**
 * Service for handling (peristent) data used by @see {@link ProjectService}.
 *
 * @author Michael Bädorf
 */
public interface ProjectServiceRepository {

    /**
     * Load base catalog of a requested version.
     *
     * @param version Version of base catalogs to load
     * @return loaded base catalog
     */
    Catalog<BaseRequirement> getBaseCatalog(String version);

    /**
     * Persists provided project wPersistierung einese neuen Projektes.
     *
     * @param project project to create
     * @return created project
     */
    Project createProject(Project project);

    /**
     * Delete a project.
     *
     * @param project identifier for project to delete
     * @return true, if project is deleted
     */
    boolean deleteProject(String project);

    /**
     * Load a persisted project.
     *
     * @param project identifier of project to load
     * @return loaded project
     */
    Optional<Project> getProject(String project);

    /**
     * Add tailoring to project.
     *
     * @param project   identifier of project to add tailoring
     * @param tailoring Tailoring to add
     * @return added tailoring
     */
    Optional<Tailoring> addTailoring(String project, Tailoring tailoring);

    /**
     * Load core project data of all projects.
     *
     * @return All projects with core data set
     */
    Collection<ProjectInformation> getProjectInformations();

    /**
     * Load core data of requested project.
     *
     * @param project identifier of project
     * @return loaded project core data
     */
    Optional<ProjectInformation> getProjectInformation(String project);

    /**
     * Load screeningsheet row data (file) of requested project.
     *
     * @param project identifier of project to get screeningsheet of
     * @return screeningsheet of project
     */
    Optional<byte[]> getScreeningSheetFile(String project);

    /**
     * Load all extracted data of screeningsheet.<p>
     * <strong>Raw data (file) not part of result!</strong>
     *
     * @param project identifier of project to get screeningsheet of
     * @return ScreeningSheet data without file raw data
     */
    Optional<ScreeningSheet> getScreeningSheet(String project);

    /**
     * Updates state of project.
     *
     * @param project project to update state of
     * @param state   state to set
     */
    Optional<ProjectInformation> updateState(String project, ProjectState state);

    /**
     * Checks, if project already exists
     * @param project identifier to project to check
     * @return true if identifier is used by an existing project
     */
    boolean isExistingProject(String project);
}
