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
import eu.tailoringexpert.domain.File;

import java.util.Optional;

public interface CatalogService {

    /**
     * Importiert den übergebenen Catalog und setzt ihn sofort gültig.
     *
     * @param catalog Der zu importierende Catalog
     * @return true, der Catalog konnte importiert werden, sonst false
     */
    boolean doImport(Catalog<BaseRequirement> catalog);

    /**
     * Ermittelt eine Catalog.
     *
     * @param version Version des zu ermittelnden Katalogs.
     * @return Sofern vorhanden, der geladenen Catalog, sonst empty
     */
    Optional<Catalog<BaseRequirement>> getCatalog(String version);

    /**
     * Erzeugt eine Druckbare Version des Katalogs.
     *
     * @param version Version des zu druckenden Katalogs.
     * @return Sofern vorhanden, die erstellte File des Catalog, sonst empty
     */

    Optional<File> createKatalog(String version);

}
