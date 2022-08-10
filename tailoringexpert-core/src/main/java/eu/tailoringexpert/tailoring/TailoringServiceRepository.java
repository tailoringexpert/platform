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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.Dokument;
import eu.tailoringexpert.domain.DokumentZeichnung;
import eu.tailoringexpert.domain.Projekt;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelektionsVektorProfil;
import eu.tailoringexpert.domain.Tailoring;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository für den Zugriff auf Projektphase Daten.
 *
 * @author baed_mi
 */
public interface TailoringServiceRepository {

    /**
     * Ermittlung eines Projektes anhand des Projektkürzels (fachlicher Schlüssel).
     *
     * @param projekt fachlicher Schlüssel des zu ermittelenden Projektes
     * @return Das ermittelte Projekt
     */
    Optional<Projekt> getProjekt(String projekt);

    /**
     * Aktualisiert den (persistenten) Katalog dem der in des Tailoring übergebenem Katalog.
     *
     * @param projekt   Projekt, zu dem das Tailoring gehört
     * @param tailoring Tailoring, mit zu übernhemenden Katalog und Statusinformationen
     * @return Das aktualisierte Tailoring
     */
    Tailoring updateTailoring(String projekt, Tailoring tailoring);

    /**
     * Aktualisiert das (persistente) Anforderungsdokument des übergebenen Tailorings.
     *
     * @param projekt   Projekt, zu dem das Tailoring gehört
     * @param tailoring Name des Tailotings, zu dem das Dokument hinzugefügt werden soll
     * @param dokument  Das hinzuzufügende Dokument
     * @return Das aktualisierte Tailoring
     */
    Optional<Tailoring> updateAnforderungDokument(String projekt, String tailoring, Dokument dokument);

    /**
     * Ermittlung eines Tailorings mit den fachlichen Schlüsseln.
     *
     * @param projekt   Projekt, zu dem das angefragte Tailoring gehört
     * @param tailoring Name der zu ermittelnden Phase
     * @return Das ermittelte Tailoring
     */
    Optional<Tailoring> getTailoring(String projekt, String tailoring);

    /**
     * Ermittlung eines Screeningsheet eines Tailorings.
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Name des Tailorings, deren ScreeningSheet ermittelt werden soll
     * @return Das ermittelte ScreeningSheet
     */
    Optional<ScreeningSheet> getScreeningSheet(String projekt, String tailoring);

    /**
     * Ermittlung der Screeningsheet Datei eines Tailorings.
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Name des Tailorings, deren ScreeningSheet Datei ermittelt werden soll
     * @return Die ermittelte ScreeningSheet Datei
     */
    Optional<byte[]> getScreeningSheetDatei(String projekt, String tailoring);

    /**
     * Aktualisierung der übergebenen Dokumentzeichnung eines Tailorings.
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Tailoring
     * @param zeichnung zu aktualisierende Zeichnung
     * @return Die aktualisierte Zeichnung
     */
    Optional<DokumentZeichnung> updateDokumentZeichnung(String projekt, String tailoring, DokumentZeichnung zeichnung);

    /**
     * Ändert den Namen einer Phase.
     *
     * @param projekt   Projekt, dem das Tailoring zugehörig ist
     * @param tailoring Aktueller Name des Tailorings
     * @param name      Neuer Name des Tailorings
     * @return Im Falle der Aktualisierung das neue Tailoring, sonst empty
     */
    Optional<Tailoring> updateName(String projekt, String tailoring, String name);

    /**
     * Ermittlung des zur (Meta-) Tailoring gespeicherten Dokumente.
     * <p>
     * Die Rohdaten werden <strong>NICHT</strong> geladen!
     *
     * @param projekt   Projekt, dem das Tailoring zugehörig ist
     * @param tailoring Name des Tailorings
     * @return Liste der Dokumente ohne die "eigentlichen" Dateidaten
     */
    List<Dokument> getDokumentListe(String projekt, String tailoring);

    /**
     * Lädt das angefragte Dokument.
     *
     * @param projekt   Projekt, dem das Tailoring zugehörig ist
     * @param tailoring Name des Tailorings
     * @param name      Name des zu ladenden Dokuments
     * @return Rohdaten der Datei
     */
    Optional<Datei> getDokument(String projekt, String tailoring, String name);

    /**
     * Löscht das angefragte Dokument.
     *
     * @param projekt   Projekt, dem das Tailoring zugehörig ist
     * @param tailoring Name der Phase
     * @param name      Name des zu löschenden Dokuments
     * @return true, wenn gelöscht, in allen anderen Fällen false
     */
    boolean deleteDokument(String projekt, String tailoring, String name);

    /**
     * Ermittlung aller Selektionsvektor Profile.
     *
     * @return Liste aller Profile
     */
    Collection<SelektionsVektorProfil> getSelektionsVektorProfile();

    /**
     * Ermittlung der konfigurierten Standard-Dokumentzeichnungen.
     *
     * @return Collection der im System definierten Standard-Dokumentzeichner
     */
    Collection<DokumentZeichnung> getDefaultZeichnungen();

    /**
     * Löschen eines Tailorings eines Projektes
     *
     * @param projekt   Projekt, aus dem ein Tailoring gelöscht werden soll
     * @param tailoring Name des zu löschenden Tailorings
     * @return
     */
    boolean deleteTailoring(String projekt, String tailoring);
}


