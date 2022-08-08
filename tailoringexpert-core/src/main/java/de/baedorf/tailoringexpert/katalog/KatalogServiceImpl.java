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

import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Optional.empty;

@Log4j2
@RequiredArgsConstructor
public class KatalogServiceImpl implements KatalogService {

    @NonNull
    private KatalogServiceRepository repository;

    @NonNull
    private DokumentService dokumentService;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doImport(@NonNull Katalog<KatalogAnforderung> katalog) {
        final ZonedDateTime jetzt = ZonedDateTime.now();
        return repository.createKatalog(katalog, jetzt).isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Katalog<KatalogAnforderung>> getKatalog(@NonNull String version) {
        return repository.getKatalog(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Datei> createKatalog(String version) {
        @SuppressWarnings("PMD.PrematureDeclaration")
        final LocalDateTime erstellungsZeitpunkt = LocalDateTime.now();
        log.info("STARTED | trying to create output document of catalogue version {}", version);

        Optional<Katalog<KatalogAnforderung>> katalog = repository.getKatalog(version);
        if (katalog.isEmpty()) {
            log.info("FINISHED | output document NOT created due to non existing catalogue version");
            return empty();
        }

        Optional<Datei> result = dokumentService.createKatalog(katalog.get(), erstellungsZeitpunkt);
        result.ifPresentOrElse(datei -> log.info("FINISHED | created output document {} of katalog version ", version),
            () -> log.info("FINISHED | output document NOT created"));

        return result;
    }


}
