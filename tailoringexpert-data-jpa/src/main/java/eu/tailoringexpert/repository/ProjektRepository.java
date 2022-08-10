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

import eu.tailoringexpert.domain.ProjektEntity;
import eu.tailoringexpert.domain.TailoringEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjektRepository extends JpaRepository<ProjektEntity, Long> {
    /**
     * Lädt ein Projekt mit seinem fachlichen Schlüssel
     *
     * @param projekt Das zu ladende Projekt
     * @return Das geladene Projekt
     */
    ProjektEntity findByKuerzel(String projekt);

    /**
     * Löscht das übergebene Projekt.
     *
     * @param projekt Das zu löschende Projekt
     * @return Anzahl der gelöschten Projekte
     */
    Long deleteByKuerzel(String projekt);

    /**
     * Ermittlung einer Pojektphase eines Projektes.
     *
     * @param projekt Projekt, dem die Projektphase zugehörig ist
     * @param name   Name der gesuchten Projektphase
     * @return Die Projektphase
     */
    @Query("Select pp from #{#entityName} p inner join p.tailorings pp where p.kuerzel=:projekt and pp.name=:name")
    TailoringEntity findTailoring(@Param("projekt") String projekt, @Param("name") String name);

}
