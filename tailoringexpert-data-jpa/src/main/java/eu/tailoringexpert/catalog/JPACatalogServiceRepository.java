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

import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseCatalogVersionProjection;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.CatalogVersion;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.DRDRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;

import jakarta.transaction.Transactional;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link CatalogServiceRepository}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class JPACatalogServiceRepository implements CatalogServiceRepository {

    @NonNull
    private JPACatalogServiceRepositoryMapper mapper;

    @NonNull
    private BaseCatalogRepository baseCatalogRepository;

    @NonNull
    private DRDRepository drdRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @CacheEvict(cacheNames = {BaseCatalogRepository.CACHE_BASECATALOGLIST})
    @Transactional
    public Optional<Catalog<BaseRequirement>> createCatalog(Catalog<BaseRequirement> catalog, ZonedDateTime validFrom) {
        log.traceEntry();

        if (isNull(catalog)) {
            return empty();
        }

        Collection<DRD> drds = apply(catalog.getToc());
        drds.forEach(domain -> {
            DRDEntity entity = drdRepository.findByNumber(domain.getNumber());
            if (isNull(entity)) {
                drdRepository.save(mapper.createCatalog(domain));
            }
        });

        BaseCatalogEntity toSave = mapper.createCatalog(catalog);
        Optional<Catalog<BaseRequirement>> result = ofNullable(mapper.createCatalog(baseCatalogRepository.save(toSave)));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Catalog<BaseRequirement>> getCatalog(String version) {
        log.traceEntry(version);

        BaseCatalogEntity entity = baseCatalogRepository.findByVersion(version);
        Optional<Catalog<BaseRequirement>> result = ofNullable(mapper.getCatalog(entity));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsCatalog(String version) {
        log.traceEntry(() -> version);
        return log.traceExit(baseCatalogRepository.existsByVersion(version));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<CatalogVersion> getCatalogVersions() {
        log.traceEntry();

        List<CatalogVersion> result = baseCatalogRepository.findCatalogVersionBy()
            .stream()
            .map(catalog -> mapper.getCatalogVersions(catalog))
            .toList();

        return log.traceExit(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CatalogVersion> limitCatalogValidity(String version, ZonedDateTime validUntil) {
        log.traceEntry(() -> version, () -> validUntil);

        int changedItems = baseCatalogRepository.setValidUntilForVersion(version, validUntil);
        if (changedItems == 0) {
            return empty();
        }
        BaseCatalogVersionProjection updatedCatalog = baseCatalogRepository.findCatalogByVersion(version);
        CatalogVersion result = mapper.limitCatalogValidity(updatedCatalog);
        return log.traceExit(ofNullable(result));
    }

    /**
     * Select all DRDs referenced in requirements of chapter and all subchapters.
     *
     * @param chapter root chapter to start selecting
     * @return Collection of referenced DRDs
     */
    private Collection<DRD> apply(Chapter<BaseRequirement> chapter) {
        return chapter.allRequirements()
            .map(BaseRequirement::getDrds)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }
}
