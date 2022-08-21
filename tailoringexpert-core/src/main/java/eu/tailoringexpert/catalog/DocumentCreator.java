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

import java.util.Map;

public interface DocumentCreator {

    /**
     * Erzeugt einen druckbaren Gesamtkatalog.
     *
     * @param docId       zu verwendender Dokumentenidentifkator
     * @param catalog     Der Catalog, zu dem das Dokument erstellt werden soll
     * @param placeholders Zusätzliche Parameter/Platzhalter, die nicht Teil des Projektphase sind
     * @return Druckbarer Dokument
     */
    File createDocument(String docId, Catalog<BaseRequirement> catalog, Map<String, String> placeholders);
}
