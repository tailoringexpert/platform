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

import eu.tailoringexpert.domain.SelectionVectorProfile;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link CatalogServiceRepository}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class JPADocumentServiceRepository implements DocumentServiceRepository {

    @NonNull
    private JPADocumentServiceRepositoryMapper mapper;

    @NonNull
    private SelectionVectorProfileRepository selectionVectorProfileRepository;

    @Override
    public Collection<SelectionVectorProfile> getSelectionVectorProfiles() {
        log.traceEntry();

        List<SelectionVectorProfile> result = selectionVectorProfileRepository.findAll()
            .stream()
            .map(mapper::getSelectionVectorProfiles)
            .toList();

        log.traceExit();
        return result;
    }

}
