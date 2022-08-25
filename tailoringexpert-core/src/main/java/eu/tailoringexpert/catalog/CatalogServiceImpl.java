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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.File;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.util.Optional.empty;

@Log4j2
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    @NonNull
    private CatalogServiceRepository repository;

    @NonNull
    private DocumentService documentService;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doImport(@NonNull Catalog<BaseRequirement> catalog) {
        final ZonedDateTime jetzt = ZonedDateTime.now();
        return repository.createCatalog(catalog, jetzt).isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Catalog<BaseRequirement>> getCatalog(@NonNull String version) {
        return repository.getCatalog(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createCatalog(String version) {
        @SuppressWarnings("PMD.PrematureDeclaration")
        final LocalDateTime creationTimestamp = LocalDateTime.now();
        log.info("STARTED | trying to create output document of catalogue version {}", version);

        Optional<Catalog<BaseRequirement>> catalog = repository.getCatalog(version);
        if (catalog.isEmpty()) {
            log.info("FINISHED | output document NOT created due to non existing catalogue version");
            return empty();
        }

        Optional<File> result = documentService.createCatalog(catalog.get(), creationTimestamp);
        result.ifPresentOrElse(datei -> log.info("FINISHED | created output document {} of catalog version ", version),
            () -> log.info("FINISHED | output document NOT created"));

        return result;
    }

}
