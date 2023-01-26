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

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.BaseRequirement;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

/**
 * Proxy for providing tenant implementations of {@link DocumentService}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class TenantDocumentService implements DocumentService {

    private final Map<String, DocumentService> tenantService;

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Optional<File> createCatalog(Catalog<BaseRequirement> catalog, LocalDateTime creationTimestamp) {
        DocumentService service = getTenantImplementation();
        return service.createCatalog(catalog, creationTimestamp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public Collection<File> createAll(Catalog<BaseRequirement> catalog, LocalDateTime creationTimestamp) {
        DocumentService service = getTenantImplementation();
        return service.createAll(catalog, creationTimestamp);
    }

    private DocumentService getTenantImplementation() throws NoSuchMethodException {
        DocumentService result = tenantService.get(TenantContext.getCurrentTenant());
        if (isNull(result)) {
            throw new NoSuchMethodException("Tenant " + TenantContext.getCurrentTenant() + " does not implement "+ DocumentService.class.getName());
        }
        return result;
    }
}
