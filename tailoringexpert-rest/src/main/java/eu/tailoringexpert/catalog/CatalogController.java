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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.BaseCatalogVersionResource;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.MediaTypeProvider;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VERION_JSON;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VERION_PDF;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VERSION;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.notFound;

/**
 * REST-Controller for management of base catalogs.
 *
 * @author Michael Bädorf
 */
@Tag(name = "Catalog Controller", description = "Managements of base catalogs")
@RequiredArgsConstructor
@RestController
public class CatalogController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private CatalogService catalogService;

    @NonNull
    private BaseCatalogRepository baseCatalogRepository;

    @NonNull
    private Function<String, MediaType> mediaTypeProvider;

    @NonNull
    private ObjectMapper objectMapper;

    @Operation(summary = "Import new base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Catalog imported"),
        @ApiResponse(
            responseCode = "400", description = "Catalog not imported")
    })
    @PostMapping(value = BASECATALOG, produces = {"application/hal+json"})
    public ResponseEntity<Void> importCatalog(
        @Parameter(description = "Base catalog to import") @RequestBody Catalog<BaseRequirement> catalog) {
        return ResponseEntity
            .status(catalogService.doImport(catalog) ? CREATED : BAD_REQUEST)
            .build();
    }

    @Operation(summary = "Retrieve all base catalog versions")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Base catalogs loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = BaseCatalogVersionResource.class)))
    })
    @GetMapping(value = BASECATALOG, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<EntityModel<BaseCatalogVersionResource>>> getCatalogs() {
        PathContextBuilder pathContext = PathContext.builder();
        List<EntityModel<BaseCatalogVersionResource>> kataloge = baseCatalogRepository.findCatalogVersionBy()
            .stream()
            .map(katalog -> EntityModel.of(mapper.toResource(pathContext, katalog)))
            .collect(toList());

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(kataloge));
    }

    @Operation(summary = "Load base catalog of requested version")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Requested base catalog loaded",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Catalog.class))),
        @ApiResponse(
            responseCode = "404", description = "Base catalog does not exist",
            content = @Content)
    })
    @GetMapping(value = BASECATALOG_VERSION, produces = {"application/json"})
    public ResponseEntity<Catalog<BaseRequirement>> getCatalog(
        @Parameter(description = "Requested base catalog version") @PathVariable String version) {
        return catalogService.getCatalog(version)
            .map(katalog -> ResponseEntity
                .ok()
                .body(katalog))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Create a printable base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Output document created"),
        @ApiResponse(
            responseCode = "404", description = "Base catalog does not exist")
    })
    @GetMapping(BASECATALOG_VERION_PDF)
    @ResponseBody
    public ResponseEntity<byte[]> getPrintCatalog(
        @Parameter(description = "Requested base catalog version") @PathVariable String version) {
        return catalogService.createCatalog(version)
            .map(dokument -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Create a JSON base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "JSON created"),
        @ApiResponse(
            responseCode = "404", description = "Base catalog does not exist")
    })
    @GetMapping(BASECATALOG_VERION_JSON)
    @ResponseBody
    public ResponseEntity<byte[]> getJsonKatalog(
        @Parameter(description = "Requested base catalog version") @PathVariable String version) throws JsonProcessingException {
        Optional<Catalog<BaseRequirement>> result = catalogService.getCatalog(version);
        if (result.isEmpty()) {
            return notFound().build();
        }

        byte[] data = objectMapper.writeValueAsBytes(result.get());
        return ResponseEntity
            .ok()
            .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename("katalog_v" + version + ".json").build().toString())
            .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
            .contentType(mediaTypeProvider.apply("json"))
            .contentLength(data.length)
            .body(data);

    }
}
