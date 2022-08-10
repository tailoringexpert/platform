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
import eu.tailoringexpert.domain.DokumentZeichnung;
import eu.tailoringexpert.domain.Kapitel;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelektionsVektor;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringAnforderung;
import eu.tailoringexpert.domain.TailoringInformation;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TailoringService {

    /**
     * Erzeugt ein neues Tailoring einschliesslich des Angforderungkatalogs mit den Parametern der übergebenen ScreeningSheet (PDF)Daten.<p>
     * Eine Projektphase kann sich über mehrere Phasen erstrecken.
     *
     * @param name                          Name des Tailorings
     * @param kennung                       Für DocId zu verwendende Kenneung
     * @param screeningSheet                ScreeningSheet der Phase
     * @param anzuwendenderSelektionsVektor Der anzuwendende Selektionsvektor
     * @return Das erstellte Tailoring
     */
    Tailoring createTailoring(String name, String kennung, ScreeningSheet screeningSheet, SelektionsVektor anzuwendenderSelektionsVektor, Katalog<KatalogAnforderung> katalog);

    /**
     * Fügt ein neues Dokument einem Tailoring eines Projektes hinzu.
     *
     * @param projekt   Projekt, zu dem das Dokument hinzugefügt werden soll
     * @param tailoring Tailoring, zu dem das Dokument hinzugefügt werden soll
     * @param name      Name des hinzuzufügenden Dokuments
     * @param daten     Raw-Daten des Dokumentes/das Datei
     * @return Tailoring, zu der das Dokument hinzugefügt wurde
     */
    Optional<Tailoring> addAnforderungDokument(String projekt, String tailoring, String name, byte[] daten);

    /**
     * Erstellt ein Anforderungsdokument für das angegebenen Tailoring des Projektes.
     *
     * @param projekt   Projektschlüssel, zu dem die Phase gehört
     * @param tailoring Identifikator des Tailorings, zu der das Dokument erstellt werden soll
     * @return Das erstellte Dokument
     */
    Optional<Datei> createAnforderungDokument(String projekt, String tailoring);

    /**
     * Erstellt einen Dokument für den Vergleich zwischen ausgewählten Anforderungen zwischen automatischen
     * Tailoring und Nachbearbeitung.
     *
     * @param projekt   Projektschlüssel, zu dem die Phase gehört
     * @param tailoring Identifikator des Tailorings, zu der das Dokument erstellt werden soll
     * @return Das erstellte Dokument
     */
    Optional<Datei> createVergleichsDokument(String projekt, String tailoring);

    /**
     * Erstellt alle Dokumente eines Tailorinngs und fasst diese in einer Zip-Datei zusammen.
     *
     * @param projekt   Projektschlüssel, zu dem die Phase gehört
     * @param tailoring Identifikator des Tailorings
     * @return Zip mit allen erstellten Dokumenten
     */
    Optional<Datei> createDokumente(String projekt, String tailoring);

    /**
     * Ermittlung des Anforderungskataloges des Tailorings eines Projektes.
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Tailoring, dessen Anforderungskatalog ermittelt werden soll
     * @return Der ermittelte Anforderungskatalog
     */
    Optional<Katalog<TailoringAnforderung>> getKatalog(String projekt, String tailoring);

    /**
     * Ermittlung der Anforderungen eines Kapitels eines Tailorings..
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Tailoring
     * @param kapitel   Kapitel, für die die Anforderungen ermittelt werden sollen
     * @return Alle Anforderungen des Kapitels
     */
    Optional<List<TailoringAnforderung>> getAnforderungen(String projekt, String tailoring, String kapitel);

    /**
     * Ermittlung des ScreeningSheets eines Tailorings..
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Tailoring
     * @return ScreeningSheet der Projektphase
     */
    Optional<ScreeningSheet> getScreeningSheet(String projekt, String tailoring);

    /**
     * Ermittlung des angwendeten Selektionsvektors eines Tailorings.
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Tailoring
     * @return Angewendeter Selektionsvektor der Projektphase
     */
    Optional<SelektionsVektor> getSelektionsVektor(String projekt, String tailoring);

    /**
     * Ermittlung des Kapitels eines Tailorings.
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Tailoring
     * @param kapitel   das zu ermittelnde Kapitel
     * @return Alle Kapiteldaten
     */
    Optional<Kapitel<TailoringAnforderung>> getKapitel(String projekt, String tailoring, String kapitel);

    /**
     * Ermittling der definierten Dokumentzeichnungen eines Tailorings.
     *
     * @param projekt   Projektschlüssel
     * @param tailoring Tailoring
     * @return Alle Dokumentzeichnungen einer Projektphase
     */
    Optional<Collection<DokumentZeichnung>> getDokumentZeichnungen(String projekt, String tailoring);

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
     * Ändert den Namen eines Tailorings.
     *
     * @param projekt   Projekt, dem das Tailoring zugehörig ist
     * @param tailoring Aktueller Name des Tailorings
     * @param name      Neuer Name des Tailorings
     * @return Im Falle der Aktualisierung das neue Tailoring, sonst empty
     */
    Optional<TailoringInformation> updateName(String projekt, String tailoring, String name);

    /**
     * Ändert den ausgewählt Status der Anforderungen des übergebenen Byte Arrays.
     *
     * @param projekt   fachlicher Identifikator des Projekts
     * @param tailoring Tailoring, deren Anforderungsauswahl geändert werden sollen
     * @param data      ByteArray mit Einträgen für Kapitel, Position und Status
     */
    void updateAusgewaehlteAnforderungen(String projekt, String tailoring, byte[] data);

    /**
     * Löschen des übergebenen Tailorings.
     *
     * @param projekt   Projekt, in der zu löschenden Projektphase
     * @param tailoring Das zu löschende Tailoring
     * @return
     */
    Optional<Boolean> deleteTailoring(String projekt, String tailoring);
}
