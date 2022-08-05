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
package de.baedorf.tailoringexpert.projekt;

import de.baedorf.tailoringexpert.domain.PathContext;
import de.baedorf.tailoringexpert.domain.PathContext.PathContextBuilder;
import de.baedorf.tailoringexpert.domain.ProjektInformation;
import de.baedorf.tailoringexpert.domain.ProjektInformationResource;
import de.baedorf.tailoringexpert.domain.ProjektResource;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.domain.ScreeningSheetResource;
import de.baedorf.tailoringexpert.domain.SelektionsVektorResource;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.tailoring.TailoringController;
import de.baedorf.tailoringexpert.tailoring.TailoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.baedorf.tailoringexpert.domain.ResourceMapper.PROJEKT;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.PROJEKTSCREENINGSHEETDATEI;
import static org.springframework.hateoas.EntityModel.of;
import static org.springframework.hateoas.server.mvc.BasicLinkBuilder.linkToCurrentMapping;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.ResponseEntity.notFound;

@Log4j2
@AllArgsConstructor
@Tag(name = "Projekt Controller", description = "Verwaltung von Projekten")
@RestController
public class ProjektController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private ProjektService projektService;

    @NonNull
    private ProjektServiceRepository projektServiceRepository;

    @NonNull
    private TailoringService tailoringService;

    @Operation(summary = "Ermittlung der Basisinformationen aller Projekte")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Liste wurde erstellt",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjektInformationResource.class))))
    })
    @GetMapping(value = ResourceMapper.PROJEKTE, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<EntityModel<ProjektInformationResource>>> getProjekte() {
        List<EntityModel<ProjektInformationResource>> projekte = projektServiceRepository.getProjektInformationen()
            .stream()
            .map(domain -> of(mapper.toResource(PathContext.builder(), domain)))
            .collect(Collectors.toList());
        return ResponseEntity
            .ok()
            .body(CollectionModel.of(projekte));

    }

    @Operation(summary = "Erstellung eines neuen Projekts")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Projekt wurde angelegt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = Void.class)))
    })
    @PostMapping(value = ResourceMapper.PROJEKT_NEU, produces = {"application/hal+json"})
    @SneakyThrows
    public ResponseEntity<Void> createProjekt(
        @Parameter(description = "Für das Projekt zu verwendende Katalogversion")
        @PathVariable("version") String version,
        @Parameter(description = "Für das Projekt zu verwendende Katalogversion")
        @RequestBody ProjektAnlageRequest request) {
        CreateProjectTO projekt = projektService.createProjekt(version, request.getScreeningSheet().getData(), request.getSelektionsVektor());
        return ResponseEntity
            .created(mapper.createLink(ResourceMapper.REL_SELF, linkToCurrentMapping().toString(), PROJEKT, Map.of("projekt", projekt.getProjekt())).toUri())
            .build();

    }

    @Operation(summary = "Ermittlung des angefragten Projektes")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Projekt wurde gefunden",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = ProjektInformationResource.class)))
    })
    @GetMapping(value = PROJEKT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<ProjektInformationResource>> getProjekt(
        @Parameter(description = "fachlicher Projektschlüssel")
        @PathVariable("projekt") String projekt) {

        PathContextBuilder pathContextBuilder = PathContext.builder();
        Optional<ProjektInformation> result = projektServiceRepository.getProjektInformation(projekt);
        result.ifPresent(pi -> pathContextBuilder.katalog(pi.getKatalogVersion()));

        return result.map(pi -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContextBuilder, pi))))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Ermittlung eines Projekt Screeningsheets")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = ScreeningSheetResource.class)))
    })
    @GetMapping(value = ResourceMapper.PROJEKTSCREENINGSHEET, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<ScreeningSheetResource>> getScreeningSheet(
        @Parameter(description = "fachlicher Projektschlüssel")
        @PathVariable("projekt") String projekt) {

        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt);
        return projektServiceRepository.getScreeningSheet(projekt)
            .map(screeningSheet -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, screeningSheet))))
            .orElseGet(() -> notFound().build());


    }

    @Operation(summary = "Ermittlung der Screeningsheet Datei eines Projektes")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet Datei wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class)))
    })
    @GetMapping(PROJEKTSCREENINGSHEETDATEI)
    @ResponseBody
    public ResponseEntity<byte[]> getScreeningSheetDatei(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt) {

        return projektServiceRepository.getScreeningSheetDatei(projekt)
            .map(daten -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder("form-data").name("attachment").filename("screeningsheet.pdf").build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(daten.length)
                .body(daten))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Löschen des übergebenen Projektes")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "Projekt wurde gelöscht",
            content = @Content(schema = @Schema(implementation = Void.class))),
        @ApiResponse(
            responseCode = "400", description = "Projekt nicht vorhanden",
            content = @Content)
    })
    @DeleteMapping(value = PROJEKT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<Void>> deleteProjekt(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt) {

        boolean deleted = projektService.deleteProjekt(projekt);
        return ResponseEntity.status(deleted ? NO_CONTENT : BAD_REQUEST).build();

    }

    @Operation(summary = "Erstellung einer Projektkopie")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Projekt wurde angelegt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = ProjektResource.class)))
    })
    @PostMapping(value = PROJEKT, produces = {"application/hal+json"})
    @ResponseBody
    @SneakyThrows
    public ResponseEntity<EntityModel<ProjektResource>> copyProjekt(
        @Parameter(description = "fachlicher Projektschlüssel des zu kopierenden Projektes") @PathVariable("projekt") String projekt,
        @Parameter(description = "PDF Daten des Screeningsheets") @RequestPart("datei") MultipartFile screeningSheet) {

        return projektService.copyProjekt(projekt, screeningSheet.getBytes())
            .map(projektKopie -> ResponseEntity
                .created(mapper.createLink(ResourceMapper.REL_SELF, linkToCurrentMapping().toString(), PROJEKT, Map.of("projekt", projektKopie.getKuerzel())).toUri())
                .body(of(mapper.toResource(PathContext.builder(), projektKopie))))
            .orElseGet(() -> notFound().build());
    }


    @Operation(summary = "Hinzufügen einer neuen Projektphase")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Projektphase wurde angelegt und dem Projekt hinzugefügt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = Void.class)))
    })
    @PostMapping(value = ResourceMapper.TAILORINGS, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<Void>> addNewTailoring(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @RequestBody ProjektAnlageRequest request) {
        Optional<Tailoring> projektPhase = projektService.addTailoring(projekt, request.getKatalog(), request.getScreeningSheet().getData(), request.getSelektionsVektor());
        if (projektPhase.isEmpty()) {
            return notFound()
                .build();
        }
        return ResponseEntity
            .created(linkTo(methodOn(TailoringController.class).getProjektPhase(projekt, projektPhase.get().getName())).toUri())
            .build();

    }

    @Operation(summary = "Laden des Selektionsvektor des Projektes")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Selektionsvektor des Projektes wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = SelektionsVektorResource.class)))
    })
    @GetMapping(value = ResourceMapper.PROJEKTSELEKTIONSVEKTOR, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<SelektionsVektorResource>> getSelektionsVektor(
        @Parameter(description = "fachlicher Projektschlüssel")
        @PathVariable("projekt") String projekt) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt);
        return projektServiceRepository.getProjekt(projekt)
            .map(p -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, p.getScreeningSheet().getSelektionsVektor()))))
            .orElseGet(() -> notFound().build());


    }

}
