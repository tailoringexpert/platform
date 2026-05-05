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
package eu.tailoringexpert.matrix;

import static java.util.Optional.of;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Function;

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.File;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Function for dowloading a file.
 * 
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
public class TenantMatrixDownloadProvider implements Function<String, Optional<File>> {

    @NonNull
    private String basedir;

    @Override
    public Optional<File> apply(@NonNull String name) {
        Path fqn = Paths.get(this.basedir, TenantContext.getCurrentTenant()).toAbsolutePath().resolve(name);
        return download(fqn);
    }

    private Optional<File> download(@NonNull Path fqn) {
        log.traceEntry(() -> fqn);

        byte[] data = null;
        try (InputStream is = Files.newInputStream(fqn)) {
            data = is.readAllBytes();
        } catch (Exception _) {
            log.info("file {} does not exists", fqn.getFileName().toString());
            return Optional.empty();
        }

        Optional<File> result = of(File.builder()
                .name(fqn.getFileName().toString())
                .data(data)
                .build());
        log.traceExit();

        return result;
    }

}
