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
package de.baedorf.tailoringexpert.tailoring;

import de.baedorf.tailoringexpert.ResourceException;
import de.baedorf.tailoringexpert.domain.DokumentResource;
import de.baedorf.tailoringexpert.domain.DokumentZeichnung;
import de.baedorf.tailoringexpert.domain.DokumentZeichnungResource;
import de.baedorf.tailoringexpert.domain.KatalogResource;
import de.baedorf.tailoringexpert.domain.MediaTypeProvider;
import de.baedorf.tailoringexpert.domain.PathContext;
import de.baedorf.tailoringexpert.domain.PathContext.PathContextBuilder;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.domain.ScreeningSheetResource;
import de.baedorf.tailoringexpert.domain.SelektionsVektorProfilResource;
import de.baedorf.tailoringexpert.domain.SelektionsVektorResource;
import de.baedorf.tailoringexpert.domain.TailoringAnforderungResource;
import de.baedorf.tailoringexpert.domain.TailoringInformationResource;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static de.baedorf.tailoringexpert.domain.ResourceMapper.TAILORING;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.TAILORINGANFORDERUNG;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.TAILORINGDOKUMENT;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.TAILORINGDOKUMENTDOWNLOAD;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.TAILORINGDOKUMENTKATALOG;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.TAILORINGKATALOGKAPITELANFORDERUG;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.TAILORINGSCREENINGSHEETDATEI;
import static de.baedorf.tailoringexpert.domain.ResourceMapper.TAILORINGVERGLEICHDOKUMENT;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.EntityModel.of;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.ResponseEntity.notFound;


@RequiredArgsConstructor
@Tag(name = "Tailoring Controller", description = "Verwaltung von Projekt Tailorings")
@RestController
public class TailoringController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private TailoringService tailoringService;

    @NonNull
    private TailoringServiceRepository projektPhaseServiceRepository;

    @NonNull
    private Function<String, MediaType> mediaTypeProvider;

    @Operation(summary = "Laden des Anforderungkatalog des Tailorings")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Anforderungskatalog wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Anforderungskatalog nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.TAILORINGKATALOG, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<KatalogResource>> getKatalog(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return tailoringService.getKatalog(projekt, tailoring)
            .map(projektPhase -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, projektPhase))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Laden eines Kapitels des Anforderungkatalogs")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Gruppe wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringKatalogKapitelResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Kapitel nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.TAILORINGKATALOGKAPITEL, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringKatalogKapitelResource>> getKapitel(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Kapitel, für das alle Informationen ermittelt werden sollen") @PathVariable("kapitel") String kapitel) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return tailoringService.getKapitel(projekt, tailoring, kapitel)
            .map(k -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, k))))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Laden des Screeningsheets des Tailorings")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet des Tailorings wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = ScreeningSheetResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Screeningsheet nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.TAILORINGSCREENINGSHEET, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<ScreeningSheetResource>> getScreeningSheet(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return tailoringService.getScreeningSheet(projekt, tailoring)
            .map(screeningSheet -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, screeningSheet))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Laden der Screeningsheet Datei des Tailorings")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet Datei des Tailorings wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Screeningsheet datei nicht vorhanden",
            content = @Content)
    })
    @GetMapping(TAILORINGSCREENINGSHEETDATEI)
    @ResponseBody
    public ResponseEntity<byte[]> getScreeningSheetDatei(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        return projektPhaseServiceRepository.getScreeningSheetDatei(projekt, tailoring)
            .map(daten -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename("screeningsheet.pdf").build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(daten.length)
                .body(daten))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Laden der angewendeten Selektionsvektors des Tailorings")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Angewendeter Selektionsvektor des Tailorings wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Angewendeter Selektionsvektor des Tailorings nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.TAILORINGSELEKTIONSVEKTOR, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<SelektionsVektorResource>> getSelektionsVektor(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return tailoringService.getSelektionsVektor(projekt, tailoring)
            .map(selektionsVektor -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, selektionsVektor))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Ermittlung der Daten eines Tailorings")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Projektphase vorhanden",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Projektphase nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = TAILORING, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringResource>> getProjektPhase(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return projektPhaseServiceRepository.getTailoring(projekt, tailoring)
            .map(projektPhase -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, projektPhase))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Hinzufügen eines (PDF) Anforderungsdokuments zur Projektphase")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Dokument wurde des Tailorings hinzugefügt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Projektphase nicht vorhanden",
            content = @Content)
    })
    @PostMapping(value = TAILORINGDOKUMENT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringResource>> addAnforderungDokument(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Hochzuladende Datei") @RequestPart("datei") MultipartFile datei) throws IOException {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return tailoringService.addAnforderungDokument(projekt, tailoring, datei.getOriginalFilename(), datei.getBytes())
            .map(projektPhase -> ResponseEntity
                .created(linkTo(methodOn(TailoringController.class).getDokumente(projekt, tailoring)).toUri())
                .body(of(mapper.toResource(pathContext, projektPhase))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Generierung eines neuen Anforderungsdokumentes für die Projektphase")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Dokument wurde erzeugt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Projektphase nicht vorhanden",
            content = @Content)
    })
    @GetMapping(TAILORINGDOKUMENT)
    @ResponseBody
    public ResponseEntity<byte[]> getDokumente(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        return tailoringService.createDokumente(projekt, tailoring)
            .map(dokument -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getBytes()))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Generierung eines neuen Anforderungsdokumentes für die Projektphase")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Dokument wurde erzeugt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Projektphase nicht vorhanden",
            content = @Content)
    })
    @GetMapping(TAILORINGDOKUMENTKATALOG)
    @ResponseBody
    public ResponseEntity<byte[]> getAnforderungDokument(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        return tailoringService.createAnforderungDokument(projekt, tailoring)
            .map(dokument -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getBytes()))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Ermittlung der Anforderungen eines Kapitels des Anforderungkatalogs eines Tailorings")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Anforderungskatalog wurde geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringAnforderungResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Kapitel nicht vorhanden",
            content = @Content)
    })
    @GetMapping(TAILORINGKATALOGKAPITELANFORDERUG)
    @ResponseBody
    public ResponseEntity<CollectionModel<EntityModel<TailoringAnforderungResource>>> getAnforderungen(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Identifikator des Kapitels") @PathVariable String kapitel) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring)
            .kapitel(kapitel);

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(
                tailoringService.getAnforderungen(projekt, tailoring, kapitel)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(domain -> of(mapper.toResource(pathContext, domain)))
                    .collect(toList())));
    }

    @Operation(summary = "Ermittlung aller für eine Projektphase definierten Zeichnungen für Anforderungsdokumente")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Dokumentzeichnungen wurden geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = DokumentZeichnungResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Zeichnungen des Tailorings nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.TAILORINGZEICHNUNG, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<DokumentZeichnungResource>> getDokumentZeichnungen(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(
                tailoringService.getDokumentZeichnungen(projekt, tailoring)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(domain -> mapper.toResource(pathContext, domain))
                    .collect(toList())));
    }

    @Operation(summary = "Aktualisierung einer Dokumentzeichnung eines Tailorings")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Dokumentzeichnung wurde aktualisiert",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = DokumentZeichnungResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Zeichnung nicht vorhanden",
            content = @Content)
    })
    @PutMapping(value = ResourceMapper.TAILORINGZEICHNUNGBEREICH, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<DokumentZeichnungResource>> updateDokumentZeichnung(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Identifikator der zu ändernden Zeichnung") @PathVariable("bereich") String bereich,
        @Parameter(description = "Neue Daten der Dokumentzeichnung") @RequestBody DokumentZeichnung dokumentZeichnung) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return tailoringService.updateDokumentZeichnung(projekt, tailoring, dokumentZeichnung)
            .map(zeichnung -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, zeichnung))))
            .orElseGet(() -> notFound().build());

    }


    @PutMapping(value = ResourceMapper.TAILORINGNAME, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringInformationResource>> updateName(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Neuer Name des Tailorings") @RequestBody String name) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(name);

        return tailoringService.updateName(projekt, tailoring, name)
            .map(projektPhase -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, projektPhase))))
            .orElseThrow(() -> new ResourceException(PRECONDITION_FAILED, "Name could not be updated"));

    }

    @GetMapping(value = ResourceMapper.TAILORINGZEICHNUNG + "/doks", produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<DokumentResource>> getDokumentListe(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .projekt(projekt)
            .tailoring(tailoring);

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(
                projektPhaseServiceRepository.getDokumentListe(projekt, tailoring)
                    .stream()
                    .map(domain -> mapper.toResource(pathContext, domain))
                    .collect(toList())));
    }

    @GetMapping(TAILORINGDOKUMENTDOWNLOAD)
    @ResponseBody
    public ResponseEntity<byte[]> getDokument(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Name der Datei") @PathVariable("name") String name) {
        return projektPhaseServiceRepository.getDokument(projekt, tailoring, name)
            .map(daten -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(name).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(daten.getType()))
                .contentLength(daten.getBytes().length)
                .body(daten.getBytes()))
            .orElseGet(() -> notFound().build());
    }

    @DeleteMapping(TAILORINGDOKUMENTDOWNLOAD)
    public ResponseEntity<Void> deleteDokument(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @Parameter(description = "Name der Datei") @PathVariable("name") String name) {
        return projektPhaseServiceRepository.deleteDokument(projekt, tailoring, name) ?
            ResponseEntity.ok().build() :
            notFound().build();
    }

    @Operation(summary = "Generierung Dokumentes für die Unterschiede zwischen automatischen und nachgetailorten Tailorings")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Dokument wurde erzeugt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring nicht vorhanden",
            content = @Content)
    })
    @GetMapping(TAILORINGVERGLEICHDOKUMENT)
    @ResponseBody
    public ResponseEntity<byte[]> getVergleichsdokumentDokument(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        return tailoringService.createVergleichsDokument(projekt, tailoring)
            .map(dokument -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getDocId()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getBytes()))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Ermittlung aller verfügbaren Selektionsvektor Profile")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Selektionsvektor Profile ermittelt",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = SelektionsVektorProfilResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Profile konnten nicht ermittelt werden",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.SELEKTIONSVEKTORPROFILE, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<EntityModel<SelektionsVektorProfilResource>>> getProfile() {
        PathContextBuilder pathContext = PathContext.builder();
        List<EntityModel<SelektionsVektorProfilResource>> profile = projektPhaseServiceRepository.getSelektionsVektorProfile()
            .stream()
            .map(profil -> of(mapper.toResource(pathContext, profil)))
            .collect(toList());

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(profile));
    }

    @Operation(summary = "Setzen des ausgewählt Status von Anforderungen des überegebenen Datenobjekts")
    @ApiResponse(responseCode = "202", description = "Daten der Anforderugen wurden ausgewertet")
    @PostMapping(TAILORINGANFORDERUNG)
    public ResponseEntity<EntityModel<Void>> updateAnforderungen(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring,
        @RequestPart("datei") MultipartFile datei) throws IOException {
        tailoringService.updateAusgewaehlteAnforderungen(projekt, tailoring, datei.getBytes());
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping(TAILORING)
    public ResponseEntity<EntityModel<Void>> deleteTailoring(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable("projekt") String projekt,
        @Parameter(description = "Identifikator des Tailorings") @PathVariable("tailoring") String tailoring) {
        Optional<Boolean> deleted = tailoringService.deleteTailoring(projekt, tailoring);
        return ResponseEntity.status(deleted.isPresent() ? OK : NOT_FOUND).build();
    }
}
