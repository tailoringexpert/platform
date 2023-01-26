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
package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.domain.TailoringState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data access layer of {@link ProjectEntity}.
 *
 * @author Michael Bädorf
 */
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    /**
     * Load project identified by its identifier.
     *
     * @param project identifier of project to load
     * @return loaded project
     */
    ProjectEntity findByIdentifier(String project);

    /**
     * Delete a project.
     *
     * @param project identifier of project to delete
     * @return number of deleted projects
     */
    Long deleteByIdentifier(String project);

    /**
     * Load tailoring of project.
     *
     * @param project poject identifier
     * @param name    name to tailoring to load
     * @return loadad tailoring
     */
    @Query("Select t from #{#entityName} p inner join p.tailorings t where p.identifier=:project and t.name=:name")
    TailoringEntity findTailoring(@Param("project") String project, @Param("name") String name);

    /**
     * Checks if a tailoring belongs to a dedicated project.
     *
     * @param project identifier of project
     * @param name    name of tailoring to check
     * @return true if name of the tailoring is part of project tailorings
     */
    @Query("select case when count(t)> 0 then true else false end from #{#entityName} p inner join p.tailorings t where p.identifier=:project and t.name=:name")
    boolean existsTailoring(@Param("project") String project, @Param("name") String name);

    /**
     * Loads state of requested tailoring
     *
     * @param project poject identifier
     * @param name    name to tailoring to load
     * @return loadad state
     */
    @Query("Select t.state from #{#entityName} p inner join p.tailorings t where p.identifier=:project and t.name=:name")
    TailoringState findTailoringState(@Param("project") String project, @Param("name") String name);

}
