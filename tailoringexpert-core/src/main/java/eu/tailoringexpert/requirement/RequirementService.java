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

public interface RequirementService {

    /**
     * Ändert den ausgewählt Status einer Requirement.
     *
     * @param project     Project, zum dem die Requirement gehört
     * @param tailoring   Tailoring des Projekts
     * @param chapter     Chapter, aus dem die Requirement stammt
     * @param position    Position der Requirement im Chapter
     * @param selected Der neue ausgwählt Status der Requirement
     * @return Die geänderte Requirement
     */
    Optional<TailoringRequirement> handleSelected(String project, String tailoring, String chapter, String position, Boolean selected);

    /**
     * Ändert den ausgewählt Status einer Anforderungen im Chapter sowie dessen Unterkapiteln.
     *
     * @param project     Project, zum dem die Requirement gehört
     * @param tailoring   Tailoring des Projekts
     * @param chapter     Chapter, der zu ändernden Auswahl aller  Anforderungen und Unteranforderungen
     * @param selected Der neue ausgwählt Status der Requirement
     * @return Chapter der geänderten Anforderungen
     */
    Optional<Chapter<TailoringRequirement>> handleSelected(String project, String tailoring, String chapter, Boolean selected);

    /**
     * Ändert den Text  einer Requirement.
     *
     * @param project   Project, zum dem die Requirement gehört
     * @param tailoring Tailoring des Projekts
     * @param chapter   Chapter, aus dem die Requirement stammt
     * @param position  Position der Requirement im Chapter
     * @param text      Der neue Text der Requirement
     * @return Die geänderte Requirement
     */
    Optional<TailoringRequirement> handleText(String project, String tailoring, String chapter, String position, String text);

    /**
     * Erstellt eine neue Project-Requirement im angegeben Project an der Position NACH der überegebenen Position
     *
     * @param project   Project, zum dem die Requirement hinzugefügt
     * @param tailoring Tailoring des Projekts
     * @param chapter   Chapter, in dem die Requirement hinzugefügt werden soll
     * @param position  Position, NACH der die neue Requirement im erstellt werden soll
     * @param text      Der Text der neuen Requirement
     * @return Die neu erstelte Requirement
     */
    Optional<TailoringRequirement> createRequirement(String project, String tailoring, String chapter, String position, String text);
}
