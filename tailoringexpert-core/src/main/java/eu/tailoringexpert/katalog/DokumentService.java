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
package eu.tailoringexpert.katalog;

import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DokumentService {

    /**
     * Erezeugt einen Gesamtkatalog.
     *
     * @param katalog              Katalogdaten für das Dokument
     * @param erstellungszeitpunkt Zeitpunkt der Dokumentstellung
     * @return Die erzeugte Katalogdatei
     */
    Optional<Datei> createKatalog(Katalog<KatalogAnforderung> katalog, LocalDateTime erstellungszeitpunkt);

}
