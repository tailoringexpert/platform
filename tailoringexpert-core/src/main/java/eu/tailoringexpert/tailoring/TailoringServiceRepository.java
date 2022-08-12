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

import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVectorProfile;
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
     * @param project fachlicher Schlüssel des zu ermittelenden Projektes
     * @return Das ermittelte Project
     */
    Optional<Project> getProject(String project);

    /**
     * Aktualisiert den (persistenten) Catalog dem der in des Tailoring übergebenem Catalog.
     *
     * @param project   Project, zu dem das Tailoring gehört
     * @param tailoring Tailoring, mit zu übernhemenden Catalog und Statusinformationen
     * @return Das aktualisierte Tailoring
     */
    Tailoring updateTailoring(String project, Tailoring tailoring);

    /**
     * Aktualisiert das (persistente) Anforderungsdokument des übergebenen Tailorings.
     *
     * @param project   Project, zu dem das Tailoring gehört
     * @param tailoring Name des Tailotings, zu dem das Dokument hinzugefügt werden soll
     * @param file  Das hinzuzufügende Dokument
     * @return Das aktualisierte Tailoring
     */
    Optional<Tailoring> updateFile(String project, String tailoring, File file);

    /**
     * Ermittlung eines Tailorings mit den fachlichen Schlüsseln.
     *
     * @param project   Project, zu dem das angefragte Tailoring gehört
     * @param tailoring Name der zu ermittelnden Phase
     * @return Das ermittelte Tailoring
     */
    Optional<Tailoring> getTailoring(String project, String tailoring);

    /**
     * Ermittlung eines Screeningsheet eines Tailorings.
     *
     * @param project   Projektschlüssel
     * @param tailoring Name des Tailorings, deren ScreeningSheet ermittelt werden soll
     * @return Das ermittelte ScreeningSheet
     */
    Optional<ScreeningSheet> getScreeningSheet(String project, String tailoring);

    /**
     * Ermittlung der Screeningsheet File eines Tailorings.
     *
     * @param project   Projektschlüssel
     * @param tailoring Name des Tailorings, deren ScreeningSheet File ermittelt werden soll
     * @return Die ermittelte ScreeningSheet File
     */
    Optional<byte[]> getScreeningSheetFile(String project, String tailoring);

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
     * Change name of tailoring.
     *
     * @param project   Project, dem das Tailoring zugehörig ist
     * @param tailoring Aktueller Name des Tailorings
     * @param name      Neuer Name des Tailorings
     * @return Im Falle der Aktualisierung das neue Tailoring, sonst empty
     */
    Optional<Tailoring> updateName(String project, String tailoring, String name);

    /**
     * Ermittlung des zur (Meta-) Tailoring gespeicherten Dokumente.
     * <p>
     * Die Rohdaten werden <strong>NICHT</strong> geladen!
     *
     * @param project   Project, dem das Tailoring zugehörig ist
     * @param tailoring Name des Tailorings
     * @return Liste der Dokumente ohne die "eigentlichen" Dateidaten
     */
    List<File> getFileList(String project, String tailoring);

    /**
     * Lädt das angefragte Dokument.
     *
     * @param project   Project, dem das Tailoring zugehörig ist
     * @param tailoring Name des Tailorings
     * @param name      Name des zu ladenden Dokuments
     * @return Rohdaten der File
     */
    Optional<File> getFile(String project, String tailoring, String name);

    /**
     * Löscht das angefragte Dokument.
     *
     * @param project   Project, dem das Tailoring zugehörig ist
     * @param tailoring Name der Phase
     * @param name      Name des zu löschenden Dokuments
     * @return true, wenn gelöscht, in allen anderen Fällen false
     */
    boolean deleteFile(String project, String tailoring, String name);

    /**
     * Ermittlung aller Selektionsvektor Profile.
     *
     * @return Liste aller Profile
     */
    Collection<SelectionVectorProfile> getSelectionVectorProfile();

    /**
     * Ermittlung der konfigurierten Standard-Dokumentzeichnungen.
     *
     * @return Collection der im System definierten Standard-Dokumentzeichner
     */
    Collection<DocumentSignature> getDefaultSignatures();

    /**
     * Löschen eines Tailorings eines Projektes
     *
     * @param project   Project, aus dem ein Tailoring gelöscht werden soll
     * @param tailoring Name des zu löschenden Tailorings
     * @return
     */
    boolean deleteTailoring(String project, String tailoring);
}


