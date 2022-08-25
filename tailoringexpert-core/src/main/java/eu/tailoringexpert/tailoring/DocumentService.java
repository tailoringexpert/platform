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

import eu.tailoringexpert.TenantInterface;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * Service für die Erzeugung von Dokumenten einer Projktphase.
 *
 * @author baed_mi
 */
@TenantInterface
public interface DocumentService {

    /**
     * Erzeugt einen neuen Anforderungskatalog für ein Tailoring.
     *
     * @param tailoring         Tailoring, für die der Catalog erstellt werden soll
     * @param creationTimestamp Zeitpunkt der Dokumentstellung
     * @return Die erzeugte Katalogdatei
     */
    Optional<File> createRequirementDocument(Tailoring tailoring, LocalDateTime creationTimestamp);

    /**
     * Erezuegt ein Vergleichsdokument mit den Unterschieden der Anforderungsselektion zwischen automatisiertem und
     * manuell nachgetailorten Anforderungen.
     *
     * @param tailoring         Projektphase, für die der Catalog erstellt werden soll
     * @param creationTimestamp Zeitpunkt der Dokumentstellung
     * @return Die erzeugte Katalogdatei
     */
    Optional<File> createComparisonDocument(Tailoring tailoring, LocalDateTime creationTimestamp);

    /**
     * Erzeugt alle Dokumente des Tailorings.
     *
     * @param tailoring         Tailoring, für die die Dokumente erstellt werden sollen
     * @param creationTimestamp Zeitpunkt der Dokumentstellung
     * @return Die erzeugte Dokumente zusammengefasst als Zip
     */
    Collection<File> createAll(Tailoring tailoring, LocalDateTime creationTimestamp);
}
