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
package de.baedorf.tailoringexpert.katalog;

import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface KatalogServiceRepository {

    /**
     * Speichert den übergebenen Katalog als den ab dem Zeitpunkt einzig gültigen Katalog.<p>
     * Bisher gültige Kataloge ab dem übergebenen Zeitpunkt ungültig markiert.
     *
     * @param katalog  Der zu erzeugende Katalog
     * @param gueligAb Beginn der Gültigkeit des neuen Katalogs
     * @return Der gespeicherte Katalog
     */
    Optional<Katalog<KatalogAnforderung>> createKatalog(Katalog<KatalogAnforderung> katalog, ZonedDateTime gueligAb);

    Optional<Katalog<KatalogAnforderung>> getKatalog(String version);
}
