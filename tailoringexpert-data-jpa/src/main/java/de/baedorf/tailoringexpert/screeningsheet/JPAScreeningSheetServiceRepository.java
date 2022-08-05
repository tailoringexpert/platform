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
package de.baedorf.tailoringexpert.screeningsheet;

import de.baedorf.tailoringexpert.domain.Parameter;
import de.baedorf.tailoringexpert.repository.ParameterRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class JPAScreeningSheetServiceRepository implements ScreeningSheetServiceRepository {
    @NonNull
    private JPAScreeningSheetServiceRepositoryMapper mapper;

    @NonNull
    private ParameterRepository parameterRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Parameter> getParameter(Collection<String> namen) {
        return parameterRepository.findByNameIn(namen)
            .stream()
            .map(mapper::toDomain)
            .collect(toList());
    }
}
