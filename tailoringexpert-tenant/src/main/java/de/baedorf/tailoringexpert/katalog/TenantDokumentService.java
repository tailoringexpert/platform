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
package de.baedorf.tailoringexpert.katalog;

import de.baedorf.tailoringexpert.TenantContext;
import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class TenantDokumentService implements DokumentService {

    private final Map<String, DokumentService> tenantService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Datei> createKatalog(Katalog<KatalogAnforderung> katalog, LocalDateTime erstellungszeitpunkt) {
        DokumentService service = tenantService.get(TenantContext.getCurrentTenant());
        if (isNull(service)) {
            return Optional.empty();
        }
        return service.createKatalog(katalog, erstellungszeitpunkt);
    }
}
