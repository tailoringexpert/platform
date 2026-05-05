/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2024 Michael Bädorf and others
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
package eu.tailoringexpert.matrix;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.MatrixFile;
import eu.tailoringexpert.domain.MatrixFileMeta;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.ObjectMapper;

@Log4j2
@RequiredArgsConstructor
public class TenantMatrixService implements MatrixService {

    @NonNull
    String basedir;

    @NonNull
    MessageDigest md;

    @NonNull
    ObjectMapper objectMapper;

    @NonNull
    Function<String, Optional<File>> download;

    private static final String SUFFIX_METAFILE = ".json";

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MatrixFileMeta> list() {
        log.traceEntry();

        Path dir = Paths.get(this.basedir, TenantContext.getCurrentTenant()).toAbsolutePath();

        if (!dir.toFile().exists()) {
            return emptyList();
        }

        Set<MatrixFileMeta> result = Stream.of(dir.toFile().listFiles())
                .filter(file -> file.getName().endsWith(SUFFIX_METAFILE))
                .map(file -> objectMapper.readValue(file, MatrixFileMeta.class))
                .collect(Collectors.toSet());

        return log.traceExit(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> get(@NonNull String name) {
        log.traceEntry(() -> name);

        Optional<File> result = download.apply(name);

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatrixFileMeta save(MatrixFile file) {
        log.traceEntry(() -> file.getName());

        Path targetDir = Paths.get(this.basedir, TenantContext.getCurrentTenant()).toAbsolutePath();
        try {
            Files.createDirectories(targetDir);
        } catch (Exception e) {
            throw log.throwing(new RuntimeException(e));
        }

        MatrixFileMeta result = MatrixFileMeta.builder()
                .name(file.getName())
                .hash(hash(file.getData()))
                .build();

        Path fqn = targetDir.resolve(file.getName());
        try (
                RandomAccessFile stream = new RandomAccessFile(fqn.toFile(), "rw");
                FileChannel channel = stream.getChannel();) {
            byte[] data = file.getData();
            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            buffer.put(data);
            buffer.flip();
            channel.write(buffer);

            // create meta file
            fqn = targetDir.resolve(file.getName() + SUFFIX_METAFILE);
            Files.writeString(fqn, objectMapper.writeValueAsString(
                    MatrixFileMeta.builder()
                            .description(file.getDescription())
                            .name(file.getName())
                            .catalogueVersion(file.getCatalogueVersion())
                            .hash(hash(file.getData()))
                            .creationTimestamp(ZonedDateTime.now())
                            .build()));
        } catch (IOException e) {
            throw log.throwing(new RuntimeException(e));
        }

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SneakyThrows
    public boolean delete(String name) {
        log.traceEntry(() -> name);
        Path dir = Paths.get(this.basedir, TenantContext.getCurrentTenant()).toAbsolutePath();

        Files.deleteIfExists(dir.resolve(name));
        Files.deleteIfExists(dir.resolve(name + SUFFIX_METAFILE));

        boolean result = !dir.resolve(name).toFile().exists() && !dir.resolve(name + SUFFIX_METAFILE).toFile().exists();

        return log.traceExit(result);
    }

    /**
     * Generates hash of provided data.
     *
     * @param data data to generate hash of
     * @return generated hash
     */
    private String hash(byte[] data) {
        return new BigInteger(1, md.digest(data)).toString(16);
    }

}
