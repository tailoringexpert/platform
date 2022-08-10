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

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.Tailoring;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class TenantDokumentService implements DokumentService {

    private final Map<String, DokumentService> tenantService;

    @Override
    public Optional<Datei> createAnforderungDokument(Tailoring tailoring, LocalDateTime erstellungsZeitpunkt) {
        DokumentService service = tenantService.get(TenantContext.getCurrentTenant());
        if (isNull(service)) {
            return Optional.empty();
        }
        return service.createAnforderungDokument(tailoring, erstellungsZeitpunkt);
    }

    @Override
    public Optional<Datei> createVergleichsDokument(Tailoring tailoring, LocalDateTime erstellungsZeitpunkt) {
        DokumentService service = tenantService.get(TenantContext.getCurrentTenant());
        if (isNull(service)) {
            return Optional.empty();
        }
        return service.createVergleichsDokument(tailoring, erstellungsZeitpunkt);
    }

    @Override
    public Collection<Datei> createAll(Tailoring tailoring, LocalDateTime erstellungsZeitpunkt) {
        DokumentService service = tenantService.get(TenantContext.getCurrentTenant());
        if (isNull(service)) {
            return emptyList();
        }
        return service.createAll(tailoring, erstellungsZeitpunkt);
    }
}
