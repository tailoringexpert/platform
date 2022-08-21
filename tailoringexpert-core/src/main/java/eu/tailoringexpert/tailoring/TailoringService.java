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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
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
     * @param identifier                       Für DocId zu verwendende Kenneung
     * @param screeningSheet                ScreeningSheet der Phase
     * @param applicableSelectionVector Der anzuwendende Selektionsvektor
     * @return Das erstellte Tailoring
     */
    Tailoring createTailoring(String name, String identifier, ScreeningSheet screeningSheet, SelectionVector applicableSelectionVector, Catalog<BaseRequirement> catalog);

    /**
     * Fügt ein neues Dokument einem Tailoring eines Projektes hinzu.
     *
     * @param project   Project, zu dem das Dokument hinzugefügt werden soll
     * @param tailoring Tailoring, zu dem das Dokument hinzugefügt werden soll
     * @param name      Name des hinzuzufügenden Dokuments
     * @param data     Raw-Daten des Dokumentes/das File
     * @return Tailoring, zu der das Dokument hinzugefügt wurde
     */
    Optional<Tailoring> addFile(String project, String tailoring, String name, byte[] data);

    /**
     * Erstellt ein Anforderungsdokument für das angegebenen Tailoring des Projektes.
     *
     * @param project   Projektschlüssel, zu dem die Phase gehört
     * @param tailoring Identifier des Tailorings, zu der das Dokument erstellt werden soll
     * @return Das erstellte Dokument
     */
    Optional<File> createRequirementDocument(String project, String tailoring);

    /**
     * Erstellt einen Dokument für den Vergleich zwischen ausgewählten Anforderungen zwischen automatischen
     * Tailoring und Nachbearbeitung.
     *
     * @param project   Projektschlüssel, zu dem die Phase gehört
     * @param tailoring Identifier des Tailorings, zu der das Dokument erstellt werden soll
     * @return Das erstellte Dokument
     */
    Optional<File> createComparisonDocument(String project, String tailoring);

    /**
     * Erstellt alle Dokumente eines Tailorinngs und fasst diese in einer Zip-File zusammen.
     *
     * @param project   Projektschlüssel, zu dem die Phase gehört
     * @param tailoring Identifier des Tailorings
     * @return Zip mit allen erstellten Dokumenten
     */
    Optional<File> createDocuments(String project, String tailoring);

    /**
     * Ermittlung des Anforderungskataloges des Tailorings eines Projektes.
     *
     * @param project   Projektschlüssel
     * @param tailoring Tailoring, dessen Anforderungskatalog ermittelt werden soll
     * @return Der ermittelte Anforderungskatalog
     */
    Optional<Catalog<TailoringRequirement>> getCatalog(String project, String tailoring);

    /**
     * Ermittlung der Anforderungen eines Kapitels eines Tailorings..
     *
     * @param project   Projektschlüssel
     * @param tailoring Tailoring
     * @param chapter   Chapter, für die die Anforderungen ermittelt werden sollen
     * @return Alle Anforderungen des Kapitels
     */
    Optional<List<TailoringRequirement>> getRequirements(String project, String tailoring, String chapter);

    /**
     * Ermittlung des ScreeningSheets eines Tailorings..
     *
     * @param project   Projektschlüssel
     * @param tailoring Tailoring
     * @return ScreeningSheet der Projektphase
     */
    Optional<ScreeningSheet> getScreeningSheet(String project, String tailoring);

    /**
     * Ermittlung des angwendeten Selektionsvektors eines Tailorings.
     *
     * @param project   Projektschlüssel
     * @param tailoring Tailoring
     * @return Angewendeter Selektionsvektor der Projektphase
     */
    Optional<SelectionVector> getSelectionVector(String project, String tailoring);

    /**
     * Ermittlung des Kapitels eines Tailorings.
     *
     * @param project   Projektschlüssel
     * @param tailoring Tailoring
     * @param chapter   das zu ermittelnde Chapter
     * @return Alle Kapiteldaten
     */
    Optional<Chapter<TailoringRequirement>> getChapter(String project, String tailoring, String chapter);

    /**
     * Ermittling der definierten Dokumentzeichnungen eines Tailorings.
     *
     * @param project   Projektschlüssel
     * @param tailoring Tailoring
     * @return Alle Dokumentzeichnungen einer Projektphase
     */
    Optional<Collection<DocumentSignature>> getDocumentSignatures(String project, String tailoring);

    /**
     * Aktualisierung der übergebenen Dokumentzeichnung eines Tailorings.
     *
     * @param project   Projektschlüssel
     * @param tailoring Tailoring
     * @param signature zu aktualisierende Zeichnung
     * @return Die aktualisierte Zeichnung
     */
    Optional<DocumentSignature> updateDocumentSignature(String project, String tailoring, DocumentSignature signature);

    /**
     * Ändert den Namen eines Tailorings.
     *
     * @param project   Project, dem das Tailoring zugehörig ist
     * @param tailoring Aktueller Name des Tailorings
     * @param name      Neuer Name des Tailorings
     * @return Im Falle der Aktualisierung das neue Tailoring, sonst empty
     */
    Optional<TailoringInformation> updateName(String project, String tailoring, String name);

    /**
     * Ändert den ausgewählt Status der Anforderungen des übergebenen Byte Arrays.
     *
     * @param project   fachlicher Identifier des Projekts
     * @param tailoring Tailoring, deren Anforderungsauswahl geändert werden sollen
     * @param data      ByteArray mit Einträgen für Chapter, Position und Status
     */
    void updateSelectedRequirements(String project, String tailoring, byte[] data);

    /**
     * Löschen des übergebenen Tailorings.
     *
     * @param project   Project, in der zu löschenden Projektphase
     * @param tailoring Das zu löschende Tailoring
     * @return
     */
    Optional<Boolean> deleteTailoring(String project, String tailoring);
}
