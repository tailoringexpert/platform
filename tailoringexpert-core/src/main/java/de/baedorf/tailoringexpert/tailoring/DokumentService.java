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
package de.baedorf.tailoringexpert.tailoring;

import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Tailoring;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * Service für die Erzeugung von Dokumenten einer Projktphase.
 *
 * @author baed_mi
 */
public interface DokumentService {

    /**
     * Erzeugt einen neuen Anforderungskatalog für ein Tailoring.
     *
     * @param tailoring            Tailoring, für die der Katalog erstellt werden soll
     * @param erstellungszeitpunkt Zeitpunkt der Dokumentstellung
     * @return Die erzeugte Katalogdatei
     */
    Optional<Datei> createAnforderungDokument(Tailoring tailoring, LocalDateTime erstellungszeitpunkt);

    /**
     * Erezuegt ein Vergleichsdokument mit den Unterschieden der Anforderungsselektion zwischen automatisiertem und
     * manuell nachgetailorten Anforderungen.
     *
     * @param tailoring            Projektphase, für die der Katalog erstellt werden soll
     * @param erstellungszeitpunkt Zeitpunkt der Dokumentstellung
     * @return Die erzeugte Katalogdatei
     */
    Optional<Datei> createVergleichsDokument(Tailoring tailoring, LocalDateTime erstellungszeitpunkt);

    /**
     * Erzeugt alle Dokumente des Tailorings.
     *
     * @param tailoring            Tailoring, für die die Dokumente erstellt werden sollen
     * @param erstellungszeitpunkt Zeitpunkt der Dokumentstellung
     * @return Die erzeugte Dokumente zusammengefasst als Zip
     */
    Collection<Datei> createAll(Tailoring tailoring, LocalDateTime erstellungszeitpunkt);
}
