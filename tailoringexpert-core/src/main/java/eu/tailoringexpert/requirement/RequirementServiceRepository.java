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

public interface RequirementServiceRepository {

    /**
     * Ermittelt die durch Chapter und Position definierte Requirement.
     *
     * @param project   Project, zum dem die Requirement gehört
     * @param tailoring Tailoring des Projekts
     * @param chapter   Chapter, in der sich die Requirement befindet
     * @param position  Postion der Requirement im Chapter
     * @return Die ermittelte Requirement
     */
    Optional<TailoringRequirement> getRequirement(String project, String tailoring, String chapter, String position);

    /**
     * Ermittelt die durch das Chapter definierte Anforderungsgruppe.
     *
     * @param project   Project, zum dem die Requirement gehört
     * @param tailoring Tailoring des Projekts
     * @param chapter   Chapter der zu ermittelnden Anforderungsgruppe
     * @return Das ermittelte Chapter
     */
    Optional<Chapter<TailoringRequirement>> getChapter(String project, String tailoring, String chapter);

    /**
     * Aktualisiert die übergebene Requirement.
     *
     * @param project     Project der Requirement
     * @param tailoring   Projektphase
     * @param chapter     Chapter, zu der die Requirement gehört
     * @param requirement Die zu aktualisierende Requirement
     * @return Die aktualisierte Requirement
     */
    Optional<TailoringRequirement> updateRequirement(String project, String tailoring, String chapter, TailoringRequirement requirement);

    /**
     * Aktualisierung der ausgewaehlt Informationen aller direkten und nachfolgenden Anforderungen der Gruppe.
     *
     * @param project   Project, zum dem die Requirement gehört
     * @param tailoring Tailoring des Projekts
     * @param chapter   Chapter, deren Anforderungen aktualisiert werden sollen
     * @return Das aktualisierte Chapter
     */
    Optional<Chapter<TailoringRequirement>> updateSelected(String project, String tailoring, Chapter<TailoringRequirement> chapter);

    /**
     * Aktualisierert das übergebene Chapter.
     *
     * @param project   Project, zum dem die Requirement gehört
     * @param tailoring Tailoring des Projekts
     * @param chapter   Gruppe, deren Anforderungen aktualisiert werden sollen
     * @return Das aktualisierte Chapter
     */
    Optional<Chapter<TailoringRequirement>> updateChapter(String project, String tailoring, Chapter<TailoringRequirement> chapter);
}
