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

import eu.tailoringexpert.domain.BaseCatalogVersionResource;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static eu.tailoringexpert.domain.MediaTypeProvider.FORM_DATA;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_COMPARE_PREVIEW;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_CONVERT_EXCEL;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_COMPARE;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_PREVIEW_PDF;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VALIDUNTIL;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VERSION;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VERSION_DOCUMENT;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VERSION_EXCEL;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VERSION_JSON;
import static eu.tailoringexpert.domain.ResourceMapper.BASECATALOG_VERSION_PDF;
import static eu.tailoringexpert.domain.ResourceMapper.REL_CONVERT;
import static java.time.LocalTime.MIDNIGHT;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyMap;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;


/**
 * REST-Controller for management of base catalogs.
 *
 * @author Michael Bädorf
 */
@Tag(name = "Catalog Controller", description = "Management of base catalogs")
@Log4j2
@RequiredArgsConstructor
@RestController
public class CatalogController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private CatalogService catalogService;

    @NonNull
    private Function<String, MediaType> mediaTypeProvider;

    @NonNull
    private ObjectMapper objectMapper;

    @Operation(summary = "Import new base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Catalog imported"),
        @ApiResponse(
            responseCode = "412", description = "Catalog not imported")
    })
    @PostMapping(value = BASECATALOG, produces = {"application/hal+json"})
    public ResponseEntity<Void> postBaseCatalog(
        @Parameter(description = "Base catalog to import") @RequestBody Catalog<BaseRequirement> catalog) {
        log.traceEntry();

        ResponseEntity<Void> result = ResponseEntity
            .status(catalogService.doImport(catalog) ? CREATED : PRECONDITION_FAILED)
            .build();
        log.traceExit();
        return result;
    }

    @Operation(summary = "Retrieve all base catalog versions")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Base catalogs loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = BaseCatalogVersionResource.class)))
    })
    @GetMapping(value = BASECATALOG, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<EntityModel<BaseCatalogVersionResource>>> getBaseCatalogs() {
        log.traceEntry();

        PathContextBuilder pathContext = PathContext.builder();
        List<EntityModel<BaseCatalogVersionResource>> catalogs = catalogService.getCatalogVersions()
            .stream()
            .map(catalog -> EntityModel.of(mapper.toResource(pathContext, catalog)))
            .toList();

        ResponseEntity<CollectionModel<EntityModel<BaseCatalogVersionResource>>> result = ok()
            .body(CollectionModel.of(catalogs, mapper.createLink(REL_CONVERT, BASECATALOG_CONVERT_EXCEL, emptyMap())
            ));
        log.traceExit();
        return result;
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
    public ResponseEntity<Catalog<BaseRequirement>> getBaseCatalog(
        @Parameter(description = "Requested base catalog version") @PathVariable String version) {
        log.traceEntry();

        ResponseEntity<Catalog<BaseRequirement>> result = catalogService.getCatalog(version)
            .map(catalog -> ok().body(catalog))
            .orElseGet(() -> notFound().build());
        log.traceExit();
        return result;
    }

    @Operation(summary = "Create a printable base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Output document created",
            content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Base catalog does not exist")
    })
    @GetMapping(value = BASECATALOG_VERSION_PDF, produces = "application/octet-stream")
    public ResponseEntity<byte[]> getBaseCatalogPrint(
        @Parameter(description = "Requested base catalog version") @PathVariable String version) {
        log.traceEntry();

        ResponseEntity<byte[]> result = catalogService.createCatalog(version)
            .map(dokument -> ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());
        log.traceExit();
        return result;
    }

    @Operation(summary = "Convert (Excel) data to JSON base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Base catalog converted")
    })
    @PostMapping(value = BASECATALOG_CONVERT_EXCEL, produces = "application/json")
    public ResponseEntity<byte[]> postBaseCatalogFile(
        @RequestPart("file") MultipartFile file) throws IOException {
        log.traceEntry();

        Catalog<BaseRequirement> catalog = catalogService.doConvert(file.getBytes());

        byte[] data = objectMapper.writeValueAsBytes(catalog);
        ResponseEntity<byte[]> result = ok()
            .header(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename("catalog_" + catalog.getVersion() + ".json").build().toString())
            .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
            .contentType(mediaTypeProvider.apply("json"))
            .contentLength(data.length)
            .body(data);
        log.traceExit();
        return result;

    }

    @Operation(summary = "Load base catalog as JSON output")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Base catalog loaded"),
        @ApiResponse(
            responseCode = "404", description = "Base catalog does not exist")
    })
    @GetMapping(value = BASECATALOG_VERSION_JSON, produces = "application/json")
    public ResponseEntity<byte[]> getBaseCatalogJson(
        @Parameter(description = "Requested base catalog version") @PathVariable String version) throws JsonProcessingException {
        log.traceEntry();

        Optional<Catalog<BaseRequirement>> catalog = catalogService.getCatalog(version);
        if (catalog.isEmpty()) {
            log.traceExit();
            return notFound().build();
        }

        byte[] data = objectMapper.writeValueAsBytes(catalog.get());
        ResponseEntity<byte[]> result = ok()
            .header(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename("catalog_v" + version + ".json").build().toString())
            .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
            .contentType(mediaTypeProvider.apply("json"))
            .contentLength(data.length)
            .body(data);
        log.traceExit();
        return result;

    }

    @Operation(summary = "Load base catalog as Excel output")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Base catalog loaded"),
        @ApiResponse(
            responseCode = "404", description = "Base catalog does not exist")
    })
    @GetMapping(value = BASECATALOG_VERSION_EXCEL, produces = "application/octet-stream")
    public ResponseEntity<byte[]> getBaseCatalogExcel(
        @Parameter(description = "Requested base catalog version") @PathVariable String version) {
        log.traceEntry();

        ResponseEntity<byte[]> result = catalogService.createCatalogExcel(version)
            .map(dokument -> ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;

    }

    @Operation(summary = "Create a printable base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Output document created",
            content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Base catalog does not exist")
    })
    @GetMapping(value = BASECATALOG_VERSION_DOCUMENT, produces = "application/octet-stream")
    public ResponseEntity<byte[]> getDocuments(
        @Parameter(description = "Requested all base catalog related documents") @PathVariable String version) {
        log.traceEntry();

        ResponseEntity<byte[]> result = catalogService.createDocuments(version)
            .map(dokument -> ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());
        log.traceExit();
        return result;
    }

    @Operation(summary = "Update the valid until date of a base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Valid until date changed"),
        @ApiResponse(
            responseCode = "404", description = "Base catalog does not exist",
            content = @Content)
    })
    @PutMapping(value = BASECATALOG_VALIDUNTIL, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<BaseCatalogVersionResource>> putCatalogValidUntil(
        @Parameter(description = "Version of catalog") @PathVariable String version,
        @Parameter(description = "end date of validity") @PathVariable @DateTimeFormat(iso = DATE) LocalDate validuntil) {
        log.traceEntry();

        PathContextBuilder pathContext = PathContext.builder();

        ZonedDateTime validUntil = ZonedDateTime.of(validuntil, MIDNIGHT, systemDefault());
        ResponseEntity<EntityModel<BaseCatalogVersionResource>> result = catalogService.limitValidity(version, validUntil)
            .map(catalogVersion -> ok()
                .body(EntityModel.of(mapper.toResource(pathContext, catalogVersion))))
            .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;
    }

    @Operation(summary = "Converts JSON base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Base catalog converted")
    })
    @PostMapping(value = BASECATALOG_PREVIEW_PDF, produces = "application/octet-stream")
    public ResponseEntity<byte[]> postBaseCatalogPreview(
        @Parameter(description = "Base catalog to preview") @RequestBody Catalog<BaseRequirement> catalog) {
        log.traceEntry();

        ResponseEntity<byte[]> result = catalogService.createDocuments(catalog)
            .map(dokument -> ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;

    }

    @Operation(summary = "Deletes an existing but not used base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Catalog deleted"),
        @ApiResponse(
            responseCode = "404", description = "Catalog does not exists")
    })
    @DeleteMapping(value = BASECATALOG_VERSION)
    public ResponseEntity<Void> deleteCatalog(
        @Parameter(description = "base catalog version to delete") @PathVariable String version
    ) {
        log.traceEntry();

        ResponseEntity<Void> result = ResponseEntity
            .status(catalogService.deleteCatalog(version).isEmpty() ? NOT_FOUND : OK)
            .build();

        log.traceExit();
        return result;
    }

    @Operation(summary = "Create a printable revised base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Output document created",
            content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Base catalogs do not exist")
    })
    @GetMapping(value = BASECATALOG_COMPARE, produces = "application/octet-stream")
    public ResponseEntity<byte[]> getBaseCatalogComparePrint(
        @Parameter(description = "Baseline requirements basecatalog version") @PathVariable String base,
        @Parameter(description = "Revised requirements basecatalog version to add diffs to") @PathVariable String revised
    ) {
        log.traceEntry();

        ResponseEntity<byte[]> result = catalogService.createCatalog(base, revised)
            .map(dokument -> ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());
        log.traceExit();
        return result;
    }

    @Operation(summary = "Create a printable revised base catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Output document created",
            content = @Content(mediaType = "application/octet-stream", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Base catalogs do not exist")
    })
    @PostMapping(value = BASECATALOG_COMPARE_PREVIEW, produces = "application/octet-stream")
    public ResponseEntity<byte[]> postBaseCatalogPreviewComparePrint(
        @Parameter(description = "Baseline requirements basecatalog version") @PathVariable String base,
        @Parameter(description = "vised requirements basecatalog version to add diffs to") @RequestBody Catalog<BaseRequirement> revised) {
        log.traceEntry();

        ResponseEntity<byte[]> result = catalogService.createCatalog(base, revised)
            .map(dokument -> ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());
        log.traceExit();
        return result;
    }
}
