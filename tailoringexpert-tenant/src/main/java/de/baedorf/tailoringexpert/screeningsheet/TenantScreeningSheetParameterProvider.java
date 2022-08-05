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

import de.baedorf.tailoringexpert.TenantContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class TenantScreeningSheetParameterProvider implements ScreeningSheetParameterProvider {

    @NonNull
    private final Map<String, ScreeningSheetParameterProvider> tenantProvider;

    @Override
    public Collection<ScreeningSheetParameterEintrag> parse(InputStream is) {
        ScreeningSheetParameterProvider provider = tenantProvider.get(TenantContext.getCurrentTenant());
        if (isNull(provider)) {
            return emptyList();
        }
        return provider.parse(is);
    }
}
