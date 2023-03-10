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

/**
 * Interface for creating base catalog document.
 *
 * @author Michael Bädorf
 */
public interface DocumentCreator {

    /**
     * Create a printable document of a base catalog.
     *
     * @param docId        Identifier of document to create
     * @param catalog      Base catalog to create document of
     * @param placeholders Placeholders to use in document generation
     * @return printable document of provided base catalog document data
     */
    File createDocument(String docId, Catalog<BaseRequirement> catalog, Map<String, Object> placeholders);
}
