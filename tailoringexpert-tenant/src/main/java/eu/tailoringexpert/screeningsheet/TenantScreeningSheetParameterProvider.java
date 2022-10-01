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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Proxy for providing tenant implementations of {@link ScreeningSheetParameterProvider}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class TenantScreeningSheetParameterProvider implements ScreeningSheetParameterProvider {

    @NonNull
    private final Map<String, ScreeningSheetParameterProvider> tenantProvider;

    @Override
    @SneakyThrows
    public Collection<ScreeningSheetParameterField> parse(InputStream is) {
        ScreeningSheetParameterProvider provider = getTenantImplementation();
        return provider.parse(is);
    }

    private ScreeningSheetParameterProvider getTenantImplementation() throws NoSuchMethodException {
        ScreeningSheetParameterProvider result = tenantProvider.get(TenantContext.getCurrentTenant());
        if (isNull(result)) {
            throw new NoSuchMethodException("Tenant " + TenantContext.getCurrentTenant() + " does not implement " + ScreeningSheetParameterProvider.class.getName());
        }
        return result;
    }
}
