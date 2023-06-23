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

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * Service for handling (peristent) data used by @see {@link CatalogService}.
 *
 * @author Michael Bädorf
 */
public interface CatalogServiceRepository {

    /**
     * Persist provided base catalog and mark it as valid.<p>
     * All other valid base catalogs will be marked invalid.
     *
     * @param catalog   Base catalog to persist
     * @param validFrom Valid from of the new base catalog
     * @return persited base catalog
     */
    Optional<Catalog<BaseRequirement>> createCatalog(Catalog<BaseRequirement> catalog, ZonedDateTime validFrom);

    /**
     * Load a persisted base catalog.
     *
     * @param version Version of base catalog to load
     * @return loaded base catalog
     */
    Optional<Catalog<BaseRequirement>> getCatalog(String version);

    /**
     * Checks if base catalog of requested version already exists.
     *
     * @param version version of base catalog to check existence of
     * @return true, of base catalog exists
     */
    boolean existsCatalog(String version);

    /**
     * Loads all base catalog versions defined in system.
     *
     * @return all defined base catalog versions in system
     */
    Collection<CatalogVersion> getCatalogVersions();

    /**
     * Limits the validity of given catalog version.
     *
     * @param version    version of base catalog to limit
     * @param validUntil validity end of base catalog
     * @return Base information of limited catalog
     */
    Optional<CatalogVersion> limitCatalogValidity(String version, ZonedDateTime validUntil);
}
