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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link BaseRequirementsProviderRepository}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@Transactional
@RequiredArgsConstructor
public class JPABaseRequirementsProviderRepository implements BaseRequirementsProviderRepository {

    @NonNull
    private JPABaseRequirementsProviderRepositoryMapper mapper;

    @NonNull
    private BaseCatalogRepository baseCatalogRepository;


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Catalog<BaseRequirement>> getBaseCatalog(String version) {
        log.traceEntry(version);

        BaseCatalogEntity entity = baseCatalogRepository.findByVersion(version, BaseCatalogEntity.class);
        Optional<Catalog<BaseRequirement>> result = ofNullable(mapper.getBaseCatalog(entity));

        log.traceExit();
        return result;
    }

}
