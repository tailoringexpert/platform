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
package eu.tailoringexpert.projekt;

import eu.tailoringexpert.domain.Projekt;
import eu.tailoringexpert.domain.SelektionsVektor;
import eu.tailoringexpert.domain.Tailoring;

import java.util.Optional;

public interface ProjektService {

    /**
     * Erzeugt ein neues Projekt mit dem Katalog in der übergenbenen Version.
     *
     * @param katalogVersion                zu verwendende Katalogversion
     * @param screeningSheet                Zu verarbeitendendes ScreeningSheet
     * @param anzuwendenderSelektionsVektor Für die Selektion von Anforderungen anzuwendender Selektionsvektor
     * @return Basisdaten des erzeugten Projekts
     */
    CreateProjectTO createProjekt(String katalogVersion, byte[] screeningSheet, SelektionsVektor anzuwendenderSelektionsVektor);

    /**
     * Fügt ein neues Tailoring dem Projekt hinzu.
     *
     * @param projekt                       (Fachlicher) Schlüssel des Projektes, zu dem eine neue Phase hibzugefügt werden soll
     * @param katalog                       ZU verwendener Katalog für die Phase
     * @param screeningSheetData            Zu verarbeitendendes ScreeningSheet
     * @param anzuwendenderSelektionsVektor Für die Selektion von Anforderungen anzuwendender Selektionsvektor
     * @return hinzugefügte Projektphase
     */
    Optional<Tailoring> addTailoring(String projekt, String katalog, byte[] screeningSheetData, SelektionsVektor anzuwendenderSelektionsVektor);

    /**
     * Erstellt eine Kopie eines bereits vorhandenen Projektes.
     *
     * @param projekt        Name des zu kopierenden Projektes
     * @param screeningSheet ScreeningSheet des anzulegenden Projekts
     * @return Das neu erstellte Projekt
     */
    Optional<Projekt> copyProjekt(String projekt, byte[] screeningSheet);

    /**
     * Löscht das übergebene Projekt.
     *
     * @param projekt Name des zu löschenden Projektes
     * @return true, wenn das Projekt gelöscht wurde, false, wenn das Projekt nicht vorhanden war oder nicht gelöscht werden konnte
     */
    boolean deleteProjekt(String projekt);
}
