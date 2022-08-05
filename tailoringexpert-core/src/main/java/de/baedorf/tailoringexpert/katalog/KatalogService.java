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

import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;

import java.util.Optional;

public interface KatalogService {

    /**
     * Importiert den übergebenen Katalog und setzt ihn sofort gültig.
     *
     * @param katalog Der zu importierende Katalog
     * @return true, der Katalog konnte importiert werden, sonst false
     */
    boolean doImport(Katalog<KatalogAnforderung> katalog);

    /**
     * Ermittelt eine Katalog.
     *
     * @param version Version des zu ermittelnden Katalogs.
     * @return Sofern vorhanden, der geladenen Katalog, sonst empty
     */
    Optional<Katalog<KatalogAnforderung>> getKatalog(String version);

    /**
     * Erzeugt eine Druckbare Version des Katalogs.
     *
     * @param version Version des zu druckenden Katalogs.
     * @return Sofern vorhanden, die erstellte Datei des Katalog, sonst empty
     */

    Optional<Datei> createKatalog(String version);

}
