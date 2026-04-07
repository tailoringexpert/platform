/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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
package eu.tailoringexpert.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Function for dowloading a file.
 * 
 * @author Michael Bädorf
 */
@Log4j2
public class DownloadProvider implements Function<Path, Optional<eu.tailoringexpert.domain.File>> {

    @Override
    public Optional<eu.tailoringexpert.domain.File> apply(@NonNull Path fqn) {
        log.traceEntry(() -> fqn);

        byte[] data = null;
        try (InputStream is = Files.newInputStream(fqn)) {
            data = is.readAllBytes();
        } catch (IOException e) {
            throw log.throwing(new RuntimeException(e));
        }

        Optional<eu.tailoringexpert.domain.File> result = Optional.of(eu.tailoringexpert.domain.File.builder()
                .name(fqn.getFileName().toString())
                .data(data)
                .build());
        log.traceExit();

        return result;
    }

}
