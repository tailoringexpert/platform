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
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Implementation of {@link CatalogService}.
 *
 * @author Michael Bädorf
 */
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
        @SuppressWarnings("PMD.PrematureDeclaration") final ZonedDateTime now = ZonedDateTime.now();

        if (repository.existsCatalog(catalog.getVersion())) {
            log.info("Catalog version {} NOT imported because it already exists.", catalog.getVersion());
            return false;
        }

        Optional<Catalog<BaseRequirement>> result = repository.createCatalog(catalog, now);
        log.info("Catalog version {} {} imported", catalog.getVersion(), result.isPresent() ? " sucessful" : " not");
        return result.isPresent();
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
        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createDocuments(String version) {
        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();
        log.info("STARTED | trying to create output documents of catalogue version {}", version);

        Optional<Catalog<BaseRequirement>> catalog = repository.getCatalog(version);
        if (catalog.isEmpty()) {
            log.info("FINISHED | output documents NOT created due to non existing catalogue version");
            return empty();
        }

        Collection<File> documents = documentService.createAll(catalog.get(), creationTimestamp);
        ByteArrayOutputStream os = createZip(documents);

        return of(File.builder()
            .name("catalog_" + version + ".zip")
            .data(os.toByteArray())
            .build());
    }


    /**
     * Create zip containing provided files.
     *
     * @param documents documents files to add to zip
     * @return created zip
     */
    @SneakyThrows
    ByteArrayOutputStream createZip(Collection<File> documents) {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(result)) {
            documents.forEach(file -> addToZip(file, zip));
            return result;
        }
    }

    /**
     * Add file to zip.
     *
     * @param file file to add
     * @param zip  Zip, to add file to
     */
    @SneakyThrows
    void addToZip(File file, ZipOutputStream zip) {
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zip.putNextEntry(zipEntry);
        zip.write(file.getData(), 0, file.getData().length);
        zip.closeEntry();
    }
}
