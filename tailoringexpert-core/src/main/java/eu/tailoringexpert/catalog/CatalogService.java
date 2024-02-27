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
import eu.tailoringexpert.domain.CatalogVersion;
import eu.tailoringexpert.domain.File;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * Service for management of base catalogs.
 *
 * @author Michael Bädorf
 */
public interface CatalogService {

    /**
     * Import provided base catalog and sets immidatly valid
     *
     * @param catalog base catalog to import
     * @return true, if catalog was imported successfully
     */
    boolean doImport(Catalog<BaseRequirement> catalog);

    /**
     * Converts the given data a base catalog.<p>
     * Please note that the data file is dependent on the registered input factory.
     *
     * @param data data to convert to a basecatalog
     * @return The created basecatalog
     */
    Catalog<BaseRequirement> doConvert(byte[] data);

    /**
     * Load base catalog of requested version.
     *
     * @param version version to load
     * @return if available base catalog, else empty
     */
    Optional<Catalog<BaseRequirement>> getCatalog(String version);

    /**
     * Creates printable version of base catalog.
     *
     * @param version Version of base catalog to create document
     * @return If base catalog availabe, a printable document, else empty
     */
    Optional<File> createCatalog(String version);

    /**
     * Creates printable version of base catalog.
     *
     * @param version Version of base catalog to create document
     * @return If base catalog availabe, a printable document, else empty
     */
    Optional<File> createCatalogExcel(String version);

    /**
     * Create all documents of basecatalog.<p>
     * Documents will be provided in {@code Zip-File}.
     *
     * @param version version to load
     * @return Created zip-file containing all created documents
     */
    Optional<File> createDocuments(String version);

    /**
     * Loads all base catalog versions defined in system.
     *
     * @return all defined base catalog versions in system
     */
    Collection<CatalogVersion> getCatalogVersions();

    /**
     * Limits the validity of a base catalg.
     *
     * @param version    version of catalog to limit
     * @param validUntil end of validty of catalog
     * @return Base Information of limited catalog
     */
    Optional<CatalogVersion> limitValidity(String version, ZonedDateTime validUntil);
}
