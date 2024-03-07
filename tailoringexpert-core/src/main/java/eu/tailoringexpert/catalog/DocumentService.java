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

import eu.tailoringexpert.TenantInterface;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * Interface for providing generated document files.
 *
 * @author Michael Bädorf
 */
@TenantInterface
public interface DocumentService {

    /**
     * Create a base catalog pdf document.
     *
     * @param catalog           base catalog data for document creation
     * @param creationTimestamp timestanp of document creation
     * @return created document file
     */
    Optional<File> createCatalog(Catalog<BaseRequirement> catalog, LocalDateTime creationTimestamp);

    /**
     * Creates all documents belonging to a base catalof.
     *
     * @param catalog           base catalog data for document creation
     * @param creationTimestamp timestamp of document creation
     * @return created document {@code zip-file}
     */
    Collection<File> createAll(Catalog<BaseRequirement> catalog, LocalDateTime creationTimestamp);

    /**
     * Create a base catalog excel document.
     *
     * @param catalog           base catalog data for document creation
     * @param creationTimestamp timestanp of document creation
     * @return created document file
     */
    Optional<File> createCatalogExcel(Catalog<BaseRequirement> catalog, LocalDateTime creationTimestamp);
}
