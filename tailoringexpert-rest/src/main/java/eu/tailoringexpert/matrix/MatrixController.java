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

import static eu.tailoringexpert.domain.ResourceMapper.MATRIX;
import static eu.tailoringexpert.domain.ResourceMapper.MATRIX_FILE;
import static eu.tailoringexpert.domain.ResourceMapper.REL_SELF;
import static java.util.Collections.emptyMap;
import static org.springframework.hateoas.mediatype.hal.HalModelBuilder.emptyHalModel;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.tailoringexpert.domain.MatrixFile;
import eu.tailoringexpert.domain.MatrixFileResource;
import eu.tailoringexpert.domain.MediaTypeProvider;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ResourceMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * REST-Controller for handling matrix files.
 *
 * @author Michael Bädorf
 */
@Tag(name = "Matrix Controller", description = "Handling matrix files")
@Log4j2
@RequiredArgsConstructor
@RestController
public class MatrixController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private Function<String, MediaType> mediaTypeProvider;

    @NonNull
    private MatrixService matrixService;

    @Operation(summary = "Parse matrix file of provided raw data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matrix file parsed", content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = MatrixFile.class))),
            @ApiResponse(responseCode = "404", description = "Matrix file could not be parsed", content = @Content)
    })
    @PostMapping(value = MATRIX, produces = {
            "application/hal+json" })
    public ResponseEntity<Void> postMatrixFile(
            @Parameter(description = "Matrix file data to save") @RequestBody MatrixFile file)
            throws IOException {
        log.traceEntry();

        matrixService.save(file);
        ResponseEntity<Void> result = ResponseEntity
                .created(
                        mapper.createLink(REL_SELF, MATRIX_FILE, Map.of("name", file.getName())).toUri())
                .build();

        log.traceExit();
        return result;
    }

    @Operation(summary = "Retrieve all matrix files")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Matrix files list loaded", content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = MatrixFileResource.class)))
    })
    @GetMapping(value = MATRIX, produces = { "application/hal+json" })
    public ResponseEntity<RepresentationModel<MatrixFileResource>> getMatrixFiles() {
        log.traceEntry();

        PathContextBuilder pathContext = PathContext.builder();

        List<EntityModel<MatrixFileResource>> matrixFiles = matrixService.list()
                .stream()
                .map(meta -> EntityModel.of(mapper.toResource(pathContext, meta)))
                .toList();

        ResponseEntity<RepresentationModel<MatrixFileResource>> result = ok(
                emptyHalModel()
                        .embed(matrixFiles, LinkRelation.of("matrices"))
                        .link(mapper.createLink(REL_SELF, MATRIX, emptyMap()))
                        .build());

        log.traceExit();
        return result;
    }

    @GetMapping(value = MATRIX_FILE)
    public ResponseEntity<byte[]> getMatrixFile(
            @Parameter(description = "Name of File") @PathVariable String name) {
        log.traceEntry();

        ResponseEntity<byte[]> result = matrixService.get(name)
                .map(daten -> ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                ContentDisposition.builder(MediaTypeProvider.FORM_DATA)
                                        .name(MediaTypeProvider.MATRIX).filename(name).build().toString())
                        .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                        .contentType(mediaTypeProvider.apply(daten.getType()))
                        .contentLength(daten.getData().length)
                        .body(daten.getData()))
                .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;
    }

    @Operation(summary = "Delete matrix file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File deleted"),
            @ApiResponse(responseCode = "404", description = "File does not exist", content = @Content)
    })
    @DeleteMapping(value = MATRIX_FILE)
    public ResponseEntity<Void> deleteMatrixFile(
            @Parameter(description = "Filename") @PathVariable("name") String name) {
        log.traceEntry();

        ResponseEntity<Void> result = matrixService.delete(name) ? ok().build()
                : notFound().build();

        log.traceExit();
        return result;
    }
}
