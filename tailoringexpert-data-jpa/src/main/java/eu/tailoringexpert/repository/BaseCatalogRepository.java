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
import eu.tailoringexpert.domain.BaseCatalogVersion;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Collection;

public interface BaseCatalogRepository extends JpaRepository<BaseCatalogEntity, Long> {

    String CACHE_BASECATALOG = "BaseCatalogRepository#BaseCatalog";
    String CACHE_BASECATALOGLIST = "BaseCatalogRepository#BaseCatalogList";

    @Cacheable(CACHE_BASECATALOG)
    BaseCatalogEntity findByVersion(String version);

    @Cacheable(CACHE_BASECATALOGLIST)
    Collection<BaseCatalogVersion> findCatalogVersionBy();

    @Modifying
    @Query("update Catalog k set k.validUntil=:validUntil where k.validUntil is Null")
    int setValidUntilForEmptyValidUntil(@Param("validUntil") ZonedDateTime pointOfTime);

    @Override
    @CacheEvict(value = {CACHE_BASECATALOGLIST, CACHE_BASECATALOG}, allEntries = true)
    <S extends BaseCatalogEntity> S save(S entity);

}
