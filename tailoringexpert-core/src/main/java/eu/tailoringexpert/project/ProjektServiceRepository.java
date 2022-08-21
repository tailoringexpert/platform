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
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.Tailoring;

import java.util.Collection;
import java.util.Optional;

public interface ProjektServiceRepository {

    /**
     * Ermittelt den allgemeinen Anforderungskatalog in der übergebenen Version.
     *
     * @param version Version des Anforderungkatalogs
     * @return Anforderungskatalog in angeforderter Version
     */
    Catalog<BaseRequirement> getBaseCatalog(String version);

    /**
     * Erzeugt ein neues persistentes Project.
     *
     * @param project Das zu persistierende Project
     * @return Das persistierte Project
     */
    Project createProject(String catalog, Project project);

    /**
     * Persistierung einese neuen Projektes.
     *
     * @param project Das nei zu persistierende Project
     * @return Daten des neuen Projektes
     */
    Project createProject(Project project);

    /**
     * Löscht das übergebene Project.
     *
     * @param project Das zu löschende Project
     * @return true, wenn Project geläscht wurde
     */
    boolean deleteProjekt(String project);

    /**
     * Ermittelt das angeforderte Project.
     *
     * @param project fachlicher Schlüssel des Projekts
     * @return angefordertes Project
     */
    Optional<Project> getProject(String project);

    /**
     * Fügt eine neue Projektphase dem Prohekt hinzu.
     *
     * @param project   fachlicher Identifier des Projekts, in dem die neue Phase angelegt werden soll
     * @param tailoring Die anzulegende Projektphase
     * @return angelgete Projektphase oder empty, wenn de Phase nicht anhelegt werden konnte
     */
    Optional<Tailoring> addTailoring(String project, Tailoring tailoring);

    /**
     * Ermittlung von aller Projekte.
     *
     * @return Alle Projekte mit einem abgespecktem Satz an Projektdaten
     */
    Collection<ProjectInformation> getProjectInformations();

    /**
     * Ermittlung der minimalen Daten eines Projektes.
     *
     * @param project Fachlicher Projektschlüssel
     * @return Informationen zum angefragten Project
     */
    Optional<ProjectInformation> getProjectInformation(String project);

    /**
     * Ermittlung der ScreeningSheet File des Projektes.
     *
     * @param project Fachlicher Projektschlüssel
     * @return ScreeningSheet File der Projektes
     */
    Optional<byte[]> getScreeningSheetFile(String project);

    /**
     * Ermittelt die extrahierten und berechenten ScreeningSheet Daten.<p>
     * <strong>Die File selbst ist nicht Teil des Ergebnisses!</strong>
     *
     * @param project Project, für das die Daten geladen werden sollen
     * @return ScreeningSheet Daten ohne die Eingabedatei
     */
    Optional<ScreeningSheet> getScreeningSheet(String project);
}
