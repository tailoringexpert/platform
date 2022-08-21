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

public interface ProjectService {

    /**
     * Erzeugt ein neues Project mit dem Catalog in der übergenbenen Version.
     *
     * @param catalog                zu verwendende Katalogversion
     * @param screeningSheet                Zu verarbeitendendes ScreeningSheet
     * @param applicableSelectionVector Für die Selektion von Anforderungen anzuwendender Selektionsvektor
     * @return Basisdaten des erzeugten Projekts
     */
    CreateProjectTO createProjekt(String catalog, byte[] screeningSheet, SelectionVector applicableSelectionVector);

    /**
     * Fügt ein neues Tailoring dem Project hinzu.
     *
     * @param project                       (Fachlicher) Schlüssel des Projektes, zu dem eine neue Phase hibzugefügt werden soll
     * @param catalog                       ZU verwendener Catalog für das Tailoring
     * @param screeningSheetData            Zu verarbeitendendes ScreeningSheet
     * @param applicableSelectionVector Für die Selektion von Anforderungen anzuwendender Selektionsvektor
     * @return hinzugefügte Projektphase
     */
    Optional<Tailoring> addTailoring(String project, String catalog, byte[] screeningSheetData, SelectionVector applicableSelectionVector);

    /**
     * Erstellt eine Kopie eines bereits vorhandenen Projektes.
     *
     * @param project        Name des zu kopierenden Projektes
     * @param screeningSheet ScreeningSheet des anzulegenden Projekts
     * @return Das neu erstellte Project
     */
    Optional<Project> copyProject(String project, byte[] screeningSheet);

    /**
     * Löscht das übergebene Project.
     *
     * @param projekt Name des zu löschenden Projektes
     * @return true, wenn das Project gelöscht wurde, false, wenn das Project nicht vorhanden war oder nicht gelöscht werden konnte
     */
    boolean deleteProjekt(String projekt);
}
