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
     * Lädt ein Project mit seinem fachlichen Schlüssel
     *
     * @param project Das zu ladende Project
     * @return Das geladene Project
     */
    ProjectEntity findByIdentifier(String project);

    /**
     * Löscht das übergebene Project.
     *
     * @param project Das zu löschende Project
     * @return Anzahl der gelöschten Projekte
     */
    Long deleteByIdentifier(String project);

    /**
     * Ermittlung einer Pojektphase eines Projektes.
     *
     * @param project Project, dem die Projektphase zugehörig ist
     * @param name   Name der gesuchten Projektphase
     * @return Die Projektphase
     */
    @Query("Select pp from #{#entityName} p inner join p.tailorings pp where p.identifier=:project and pp.name=:name")
    TailoringEntity findTailoring(@Param("project") String project, @Param("name") String name);

}
