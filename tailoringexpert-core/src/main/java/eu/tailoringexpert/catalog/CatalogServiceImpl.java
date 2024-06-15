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
import eu.tailoringexpert.domain.CatalogVersion;
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
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

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

    @NonNull
    private Function<byte[], Catalog<BaseRequirement>> file2Catalog;

    private static final String MSG_CATALOGDOCUMENT_NOT_CREATED = "catalog document NOT created due to non existing catalog version.";

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doImport(@NonNull Catalog<BaseRequirement> catalog) {
        log.traceEntry(catalog::getVersion);
        @SuppressWarnings("PMD.PrematureDeclaration") final ZonedDateTime now = ZonedDateTime.now();

        if (repository.existsCatalog(catalog.getVersion())) {
            log.error("Catalog version {} NOT imported because it already exists.", catalog.getVersion());
            return log.traceExit(false);
        }

        Optional<Catalog<BaseRequirement>> result = repository.createCatalog(catalog, now);
        return log.traceExit(result.isPresent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog<BaseRequirement> doConvert(byte[] data) {
        log.traceEntry();
        return log.traceExit(file2Catalog.apply(data));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Catalog<BaseRequirement>> getCatalog(@NonNull String version) {
        log.traceEntry(() -> version);
        log.traceExit();
        return repository.getCatalog(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createCatalog(String version) {
        log.traceEntry(() -> version);
        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();

        Optional<Catalog<BaseRequirement>> catalog = repository.getCatalog(version);
        if (catalog.isEmpty()) {
            log.error(MSG_CATALOGDOCUMENT_NOT_CREATED);
            log.traceExit();
            return empty();
        }

        Optional<File> result = documentService.createCatalog(catalog.get(), creationTimestamp);
        log.traceExit();
        return result;
    }

    @Override
    public Optional<File> createDocuments(Catalog<BaseRequirement> catalog) {
        log.traceEntry();
        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();

        if (ofNullable(catalog).isEmpty()) {
            log.error(MSG_CATALOGDOCUMENT_NOT_CREATED);
            log.traceExit();
            return empty();
        }

        Collection<File> documents = documentService.createAll(catalog, creationTimestamp);
        ByteArrayOutputStream os = createZip(documents);

        File result = File.builder()
            .name("catalog_" + catalog.getVersion()+ ".zip")
            .data(os.toByteArray())
            .build();
        log.traceExit(result.getName());
        return of(result);
    }

    @Override
    public Optional<File> createCatalogExcel(String version) {
        log.traceEntry(() -> version);
        @SuppressWarnings("PMD.PrematureDeclaration") final LocalDateTime creationTimestamp = LocalDateTime.now();

        Optional<Catalog<BaseRequirement>> catalog = repository.getCatalog(version);
        if (catalog.isEmpty()) {
            log.error(MSG_CATALOGDOCUMENT_NOT_CREATED);
            log.traceExit();
            return empty();
        }

        Optional<File> result = documentService.createCatalogExcel(catalog.get(), creationTimestamp);
        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> createDocuments(String version) {
        log.traceEntry(() -> version);

        Optional<Catalog<BaseRequirement>> catalog = repository.getCatalog(version);
        Optional<File> result = createDocuments(catalog.orElse(null));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<CatalogVersion> getCatalogVersions() {
        return repository.getCatalogVersions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CatalogVersion> limitValidity(String version, ZonedDateTime validUntil) {
        if (!repository.existsCatalog(version)) {
            log.error("could not limit catalog {} because it does not exist", version);
            log.traceExit();
            return empty();
        }

        Optional<CatalogVersion> result = repository.limitCatalogValidity(version, validUntil);
        return log.traceExit(result);
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
