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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface CatalogServiceRepository {

    /**
     * Speichert den übergebenen Catalog als den ab dem Zeitpunkt einzig gültigen Catalog.<p>
     * Bisher gültige Kataloge ab dem übergebenen Zeitpunkt ungültig markiert.
     *
     * @param catalog  Der zu erzeugende Catalog
     * @param validFrom Beginn der Gültigkeit des neuen Katalogs
     * @return Der gespeicherte Catalog
     */
    Optional<Catalog<BaseRequirement>> createCatalog(Catalog<BaseRequirement> catalog, ZonedDateTime validFrom);

    Optional<Catalog<BaseRequirement>> getCatalog(String version);
}
