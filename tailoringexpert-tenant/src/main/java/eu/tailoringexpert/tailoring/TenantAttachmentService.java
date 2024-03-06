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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.File;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readString;
import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

@Log4j2
@RequiredArgsConstructor
public class TenantAttachmentService implements AttachmentService {

    @NonNull
    private BiFunction<String, String, Path> pathProvider;

    @NonNull
    private MessageDigest md;

    private static final String SUFFIX_HASHFILE = ".hash";

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> save(String project, String tailoring, File file) {
        log.traceEntry(() -> project, () -> tailoring, () -> file.getName());

        Path dir = pathProvider.apply(project, tailoring);
        if (Objects.isNull(dir)) {
            return empty();
        }

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw log.throwing(new RuntimeException(e));
        }

        File result = File.builder()
            .name(file.getName())
            .data(file.getData())
            .hash(hash(file.getData()))
            .build();

        Path fqn = dir.resolve(file.getName());
        try (
            RandomAccessFile stream = new RandomAccessFile(fqn.toFile(), "rw");
            FileChannel channel = stream.getChannel();
        ) {
            byte[] data = file.getData();
            ByteBuffer buffer = ByteBuffer.allocate(data.length);
            buffer.put(data);
            buffer.flip();
            channel.write(buffer);

            // create hashfile
            fqn = dir.resolve(file.getName() + SUFFIX_HASHFILE);
            Files.writeString(fqn, result.getHash());
        } catch (IOException e) {
            throw log.throwing(new RuntimeException(e));
        }

        log.traceExit();
        return Optional.of(result);
    }

    /**
     * {@inheritDoc}
     */
    @SneakyThrows
    @Override
    public boolean delete(String project, String tailoring, String filename) {
        log.traceEntry(() -> project, () -> tailoring, () -> filename);
        Path dir = pathProvider.apply(project, tailoring);

        boolean result = Files.deleteIfExists(dir.resolve(filename));
        Files.deleteIfExists(dir.resolve(filename + SUFFIX_HASHFILE));

        return log.traceExit(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<File> load(String project, String tailoring, String filename) {
        log.traceEntry(() -> project, () -> tailoring, () -> filename);

        Path fqn = pathProvider.apply(project, tailoring).resolve(filename);
        byte[] data = null;
        try (InputStream is = newInputStream(fqn)) {
            data = is.readAllBytes();
        } catch (IOException e) {
            throw log.throwing(new RuntimeException(e));
        }

        return Optional.of(File.builder()
            .name(filename)
            .data(data)
            .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<File> list(String project, String tailoring) {
        log.traceEntry(() -> project, () -> tailoring);

        Path dir = pathProvider.apply(project, tailoring);
        if (!dir.toFile().exists()) {
            return emptyList();
        }

        Map<String, String> hashes = getHashes(dir);
        return of(dir.toFile().listFiles())
            .filter(file -> !isHashFile(file))
            .map(file -> File.builder()
                .name(file.getName())
                .hash(hashes.get(file.getName()))
                .build()
            )
            .collect(Collectors.toSet());
    }

    /**
     * Gets all (stored) file hashes
     *
     * @param path path of  hashfiles to evaluate
     * @return mapping between file and corresponding hash
     */
    private Map<String, String> getHashes(Path path) {
        return of(path.toFile().listFiles())
            .filter(this::isHashFile)
            .map(file -> entry(toHashedFilename(file), readHash(file)))
            .collect(toMap(Entry::getKey, Entry::getValue, (a, b) -> b));

    }

    /**
     * Reads first string line of the given file.
     *
     * @param file file to read
     * @return first string line of file
     */
    @SneakyThrows
    private String readHash(java.io.File file) {
        return readString(file.toPath(), StandardCharsets.UTF_8).replaceAll("[\\r\\n]", "");
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

    private boolean isHashFile(java.io.File file) {
        return file.getName().endsWith(SUFFIX_HASHFILE);
    }

    private String toHashedFilename(java.io.File hashFile) {
        return hashFile.getName().substring(0, hashFile.getName().lastIndexOf(SUFFIX_HASHFILE));
    }
}
