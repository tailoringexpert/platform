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
package de.baedorf.tailoringexpert.anforderung;

import de.baedorf.tailoringexpert.domain.PathContext;
import de.baedorf.tailoringexpert.domain.PathContext.PathContextBuilder;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.domain.TailoringAnforderungResource;
import de.baedorf.tailoringexpert.domain.TailoringKatalogKapitelResource;
import de.baedorf.tailoringexpert.domain.TailoringResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static de.baedorf.tailoringexpert.domain.ResourceMapper.ANFORDERUNG;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.ANFORDERUNG_AUSGEWAEHLT;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.ANFORDERUNG_TEXT;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.KAPITEL_AUSGEWAEHLT;

@Log
@RequiredArgsConstructor
@Tag(name = "Anforderung Controller", description = "Verwaltung von Anforderungen")
@RestController
public class AnforderungController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private AnforderungService anforderungService;

    @NonNull
    private AnforderungServiceRepository anforderungServiceRepository;


    @Operation(summary = "Laden einer Anforderung")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Anforderung geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringAnforderungResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Anforderung nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = ANFORDERUNG, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringAnforderungResource>> getAnforderung(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Identifikator des Kapitels") @PathVariable String kapitel,
        @Parameter(description = "Position der Anforderung im Kapitel") @PathVariable String anforderung) {

        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring)
            .kapitel(kapitel);
        return anforderungServiceRepository.getAnforderung(projekt, tailoring, kapitel, anforderung)
            .map(projektPhaseAnforderung -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, projektPhaseAnforderung))))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Änderung des Selektionsstatus der Anforderungen eines Kapitels inkl. Unterkapitel")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Anforderungsstatus geändert"),
        @ApiResponse(
            responseCode = "404", description = "Anforderung nicht vorhanden",
            content = @Content)
    })
    @PutMapping(value = ANFORDERUNG_AUSGEWAEHLT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringAnforderungResource>> updateKapitelAnforderungStatus(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Identifikator des Kapitels") @PathVariable String kapitel,
        @Parameter(description = "Position der Anforderung im Kapitel") @PathVariable String anforderung,
        @Parameter(description = "Der neue Selketionsstatus der Anforderung") @PathVariable Boolean ausgewaehlt) {

        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring)
            .kapitel(kapitel)
            .anforderung(anforderung);

        return anforderungService.handleAusgewaehlt(projekt, tailoring, kapitel, anforderung, ausgewaehlt)
            .map(projektPhaseAnforderung -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, projektPhaseAnforderung))))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Änderung des Textes einer Anforderung")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Text geändert"),
        @ApiResponse(
            responseCode = "404", description = "Anforderung nicht vorhanden",
            content = @Content)
    })
    @PutMapping(ANFORDERUNG_TEXT)
    public ResponseEntity<EntityModel<TailoringAnforderungResource>> updateAnforderungText(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Identifikator des Kapitels") @PathVariable String kapitel,
        @Parameter(description = "Position der Anforderung im Kapitel") @PathVariable String anforderung,
        @Parameter(description = "Der neue Anforderungstext") @RequestBody String text) {

        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring)
            .kapitel(kapitel)
            .anforderung(anforderung);

        return anforderungService.handleText(projekt, tailoring, kapitel, anforderung, text)
            .map(projektPhaseAnforderung -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, projektPhaseAnforderung))))
            .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @Operation(summary = "Änderung der Selektionsstatus aller Anforderungen eines Kapitels")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Anforderungsstatus geändert"),
        @ApiResponse(
            responseCode = "404", description = "Kapitel nicht vorhanden",
            content = @Content)
    })
    @PutMapping(KAPITEL_AUSGEWAEHLT)
    public ResponseEntity<EntityModel<TailoringKatalogKapitelResource>> updateKapitelAnforderungStatus(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Identifikator des Kapitels") @PathVariable String kapitel,
        @Parameter(description = "Der neue Selketionsstatus der Anforderungen") @PathVariable Boolean ausgewaehlt) {

        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring)
            .kapitel(kapitel);

        return anforderungService.handleAusgewaehlt(projekt, tailoring, kapitel, ausgewaehlt)
            .map(gruppe -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, gruppe))))
            .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @Operation(summary = "Hinzufügen einer manuellen Anforderung zur Projektphase")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Anforderung wurde der Projektphase hinzugefügt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Projektphase nicht vorhanden",
            content = @Content)
    })
    @PostMapping(value = ANFORDERUNG, produces = {"application/hal+json"})
    @SneakyThrows
    public ResponseEntity<EntityModel<TailoringAnforderungResource>> createAnforderung(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Identifikator des Kapitels") @PathVariable String kapitel,
        @Parameter(description = "Position der Anforderung, NACH der die Anforderung erstellt werden soll") @PathVariable("anforderung") String anforderung,
        @Parameter(description = "Text der neuen Anforderung") @RequestBody String text) {

        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring)
            .kapitel(kapitel);
        return anforderungService.createAnforderung(projekt, tailoring, kapitel, anforderung, text)
            .map(erstellteAnforderung -> ResponseEntity
                .created(UriTemplate.of(ANFORDERUNG).expand(pathContext.build().parameter()))
                .body(EntityModel.of(mapper.toResource(pathContext, erstellteAnforderung))))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
