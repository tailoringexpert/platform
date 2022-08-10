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
package eu.tailoringexpert.katalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.domain.KatalogVersionResource;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.Katalog;
import eu.tailoringexpert.domain.KatalogAnforderung;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.MediaTypeProvider;
import eu.tailoringexpert.repository.KatalogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.util.stream.Collectors;

import static org.springframework.hateoas.EntityModel.of;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@RequiredArgsConstructor
@RestController
public class KatalogController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private KatalogService katalogService;

    @NonNull
    private KatalogRepository katalogDefinitionRepository;

    @NonNull
    private Function<String, MediaType> mediaTypeProvider;

    @NonNull
    private ObjectMapper objectMapper;

    @Operation(summary = "Import eines neuen Anforderungkatalogs")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Katalog wurde importiert")
    })
    @PostMapping(value = ResourceMapper.KATALOG, produces = {"application/hal+json"})
    public ResponseEntity<Void> importKatalog(
        @Parameter(description = "Der zu importierende Katalog") @RequestBody Katalog<KatalogAnforderung> katalog) {
        return ResponseEntity
            .status(katalogService.doImport(katalog) ? CREATED : BAD_REQUEST)
            .build();
    }

    @Operation(summary = "Ermittlung aller verfügbaren Anforderungskatalogversionen")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Katalogversionen ermittelt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = KatalogVersionResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Kataloge konnten nicht ermittelt werden",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.KATALOG, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<EntityModel<KatalogVersionResource>>> getKataloge() {
        PathContextBuilder pathContext = PathContext.builder();
        List<EntityModel<KatalogVersionResource>> kataloge = katalogDefinitionRepository.findKatalogVersionBy()
            .stream()
            .map(katalog -> of(mapper.toResource(pathContext, katalog)))
            .collect(Collectors.toList());

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(kataloge));
    }

    @Operation(summary = "Ermittlung eines Kataloges")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Katalogversionen ermittelt",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Katalog.class))),
        @ApiResponse(
            responseCode = "404", description = "Katalog exisitiert nicht",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.KATALOGVERSION, produces = {"application/json"})
    public ResponseEntity<Katalog<KatalogAnforderung>> getKatalog(@Parameter(description = "Version des angefragten Katalogs") @PathVariable("version") String version) {
        return katalogService.getKatalog(version)
            .map(katalog -> ResponseEntity
                .ok()
                .body(katalog))
            .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @GetMapping(ResourceMapper.KATALOGVERSIONPDFDOWNLOAD)
    @ResponseBody
    public ResponseEntity<byte[]> getOutputKatalog(@Parameter(description = "Version des angefragten Katalogs") @PathVariable("version") String version) {
        return katalogService.createKatalog(version)
            .map(dokument -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getBytes()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(ResourceMapper.KATALOGVERSIONJSONDOWNLOAD)
    @ResponseBody
    public ResponseEntity<byte[]> getJsonKatalog(@Parameter(description = "Version des angefragten Katalogs") @PathVariable("version") String version) throws JsonProcessingException {
        Optional<Katalog<KatalogAnforderung>> result = katalogService.getKatalog(version);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
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
