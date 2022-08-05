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
package de.baedorf.tailoringexpert.repository;

import de.baedorf.tailoringexpert.domain.KatalogEntity;
import de.baedorf.tailoringexpert.domain.KatalogVersion;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.Collection;

public interface KatalogRepository extends JpaRepository<KatalogEntity, Long> {

    String CACHE_KATALOG = "KatalogRepository#Katalog";
    String CACHE_KATALOGKLISTE = "KatalogRepository#KatalogListe";

    @Cacheable(CACHE_KATALOG)
    KatalogEntity findByVersion(String version);

    @Cacheable(CACHE_KATALOGKLISTE)
    Collection<KatalogVersion> findKatalogVersionBy();

    @Modifying
    @Query("update Katalog k set k.gueltigBis=:gueltigBis where k.gueltigBis is Null")
    int setGueltigBisFuerNichtGesetztesGueltigBis(@Param("gueltigBis") ZonedDateTime zeitpunkt);

    @Override
    @CacheEvict(value = {CACHE_KATALOGKLISTE, CACHE_KATALOG}, allEntries = true)
    <S extends KatalogEntity> S save(S entity);

}
