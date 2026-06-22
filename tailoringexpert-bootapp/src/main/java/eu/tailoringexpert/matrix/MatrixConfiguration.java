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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.ResourceMapper;
import lombok.NonNull;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class MatrixConfiguration {

    @Bean
    Function<String, Optional<File>> matrixDownload(@NonNull @Value("${tailoringexpert.home.matrix}") String basedir) {
        return new TenantMatrixDownloadProvider(basedir);
    }

    @Bean
    MatrixService matrixService(
            @NonNull @Value("${tailoringexpert.home.matrix}") String basedir,
            @NonNull @Qualifier("matrixDownload") Function<String, Optional<File>> download,
            @NonNull ObjectMapper objectMapper) throws NoSuchAlgorithmException {
        return new TenantMatrixService(basedir, MessageDigest.getInstance("SHA-256"), objectMapper, download);

    }

    @Bean
    MatrixController matrixController(@NonNull ResourceMapper mapper,
            @NonNull Function<String, MediaType> mediaTypeProvider, @NonNull MatrixService matrixService) {
        return new MatrixController(mapper, mediaTypeProvider, matrixService);
    }
}
