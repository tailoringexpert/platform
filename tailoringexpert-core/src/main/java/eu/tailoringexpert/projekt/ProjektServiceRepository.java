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

import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.Projekt;
import eu.tailoringexpert.domain.ProjektInformation;
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
    Katalog<KatalogAnforderung> getKatalog(String version);

    /**
     * Erzeugt ein neues persistentes Projekt.
     *
     * @param projekt Das zu persistierende Projekt
     * @return Das persistierte Projekt
     */
    Projekt createProjekt(String katalogVersion, Projekt projekt);

    /**
     * Persistierung einese neuen Projektes.
     *
     * @param projekt Das nei zu persistierende Projekt
     * @return Daten des neuen Projektes
     */
    Projekt createProjekt(Projekt projekt);

    /**
     * Löscht das übergebene Projekt.
     *
     * @param projekt Das zu löschende Projekt
     * @return true, wenn Projekt geläscht wurde
     */
    boolean deleteProjekt(String projekt);

    /**
     * Ermittelt das angeforderte Projekt.
     *
     * @param projekt fachlicher Schlüssel des Projekts
     * @return angefordertes Projekt
     */
    Optional<Projekt> getProjekt(String projekt);

    /**
     * Fügt eine neue Projektphase dem Prohekt hinzu.
     *
     * @param projekt   fachlicher Identifikator des Projekts, in dem die neue Phase angelegt werden soll
     * @param tailoring Die anzulegende Projektphase
     * @return angelgete Projektphase oder empty, wenn de Phase nicht anhelegt werden konnte
     */
    Optional<Tailoring> addTailoring(String projekt, Tailoring tailoring);

    /**
     * Ermittlung von aller Projekte.
     *
     * @return Alle Projekte mit einem abgespecktem Satz an Projektdaten
     */
    Collection<ProjektInformation> getProjektInformationen();

    /**
     * Ermittlung der minimalen Daten eines Projektes.
     *
     * @param projekt Fachlicher Projektschlüssel
     * @return Informationen zum angefragten Projekt
     */
    Optional<ProjektInformation> getProjektInformation(String projekt);

    /**
     * Ermittlung der ScreeningSheet Datei des Projektes.
     *
     * @param projekt Fachlicher Projektschlüssel
     * @return ScreeningSheet Datei der Projektes
     */
    Optional<byte[]> getScreeningSheetDatei(String projekt);

    /**
     * Ermittelt die extrahierten und berechenten ScreeningSheet Daten.<p>
     * <strong>Die Datei selbst ist nicht Teil des Ergebnisses!</strong>
     *
     * @param projekt Projekt, für das die Daten geladen werden sollen
     * @return ScreeningSheet Daten ohne die Eingabedatei
     */
    Optional<ScreeningSheet> getScreeningSheet(String projekt);
}
