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
package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseCatalogVersionProjection;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * Spring Data access layer of {@link BaseCatalogEntity}.
 *
 * @author Michael Bädorf
 */
public interface BaseCatalogRepository extends JpaRepository<BaseCatalogEntity, Long> {

    String CACHE_BASECATALOG = "BaseCatalogRepository#BaseCatalog";
    String CACHE_BASECATALOGLIST = "BaseCatalogRepository#BaseCatalogList";

    /**
     * Load a dedicated base catalog.
     *
     * @param version version of base catalog to load
     * @return loaded base catalog
     */
    @Cacheable(CACHE_BASECATALOG)
    <T> T findByVersion(String version, Class<T> clz);

    /**
     * Loads "pure" version and validities of all defined base catalogs.
     *
     * @return collection of base catalog versions defined in system
     */
    @Cacheable(CACHE_BASECATALOGLIST)
    Collection<BaseCatalogVersionProjection> findCatalogVersionBy();

    /**
     * Loads "pure" version and validity of requested base catalog.
     *
     * @return base catalog version of requested base catalog
     */
    BaseCatalogVersionProjection findCatalogByVersion(String version);

    /**
     * Save a base catalog.
     *
     * @param entity to save
     * @param <S>
     * @return saved base catalog
     */
    @Override
    @CacheEvict(value = {CACHE_BASECATALOGLIST, CACHE_BASECATALOG}, allEntries = true)
    <S extends BaseCatalogEntity> S save(S entity);

    /**
     * Checks if base catalog of requested version already exists.
     *
     * @param version version of base catalog to check existence of
     * @return true, of base catalog exists
     */
    boolean existsByVersion(String version);

    /**
     * Sets valid until of a requested base catalog version.
     *
     * @param version     base catalog version to update
     * @param pointOfTime end of validity
     * @return number of updated base catalogs
     */
    @Transactional
    @Modifying
    @Query("update #{#entityName} c set c.validUntil=:validUntil where c.version=:version")
    @CacheEvict(value = {CACHE_BASECATALOGLIST, CACHE_BASECATALOG}, allEntries = true)
    int setValidUntilForVersion(@Param("version") String version, @Param("validUntil") ZonedDateTime pointOfTime);

    /**
     * Deletes the requested base catalog version.
     *
     * @param version version of base catalog to delete
     */
    void deleteByVersion(String version);


}
