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
package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.SelectionVector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * Proxy for providing tenant implementations of {@link SelectionVectorProvider}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class TenantSelectionVectorProvider implements SelectionVectorProvider {

    @NonNull
    private final Map<String, SelectionVectorProvider> tenantProvider;


    @Override
    public SelectionVector apply(Collection<Parameter> parameters) {
        SelectionVectorProvider provider = tenantProvider.get(TenantContext.getCurrentTenant());
        return nonNull(provider) ? provider.apply(parameters) : null;
    }
}
