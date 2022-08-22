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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.ResourceException;
import eu.tailoringexpert.domain.FileResource;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.DocumentSignatureResource;
import eu.tailoringexpert.domain.SelectionVectorProfileResource;
import eu.tailoringexpert.domain.TailoringCatalogResource;
import eu.tailoringexpert.domain.MediaTypeProvider;
import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.ScreeningSheetResource;
import eu.tailoringexpert.domain.SelectionVectorResource;
import eu.tailoringexpert.domain.TailoringRequirementResource;
import eu.tailoringexpert.domain.TailoringResource;
import eu.tailoringexpert.domain.TailoringCatalogChapterResource;
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
import org.springframework.hateoas.UriTemplate;
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

import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_ATTACHMENTS;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_ATTACHMENT;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_CATALOG;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_CATALOG_CHAPTER;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_CATALOG_CHAPTER_REQUIREMENT;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_COMPARE;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_DOCUMENT;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_DOCUMENT_CATALOG;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SCREENINGSHEET;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SCREENINGSHEET_PDF;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SELECTIONVECTOR;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SIGNATURE;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SIGNATURE_FACULTY;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.EntityModel.of;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.ResponseEntity.notFound;


@RequiredArgsConstructor
@Tag(name = "Tailoring Controller", description = "Management of tailorings")
@RestController
public class TailoringController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private TailoringService tailoringService;

    @NonNull
    private TailoringServiceRepository tailoringServiceRepository;

    @NonNull
    private Function<String, MediaType> mediaTypeProvider;

    @Operation(summary = "Load tailoring requirements catalog")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Catalog loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringCatalogResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Catalog not be loaded",
            content = @Content)
    })
    @GetMapping(value = TAILORING_CATALOG, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringCatalogResource>> getCatalog(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.getCatalog(project, tailoring)
            .map(projektPhase -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, projektPhase))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Load all chapter with all contained requirements")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Chapter loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringCatalogChapterResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Chapter does not exits",
            content = @Content)
    })
    @GetMapping(value = TAILORING_CATALOG_CHAPTER, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringCatalogChapterResource>> getChapter(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Chapter number") @PathVariable String chapter) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.getChapter(project, tailoring, chapter)
            .map(k -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, k))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Load screeningsheet data of tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = ScreeningSheetResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Screeningsheet does not exist",
            content = @Content)
    })
    @GetMapping(value = TAILORING_SCREENINGSHEET, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<ScreeningSheetResource>> getScreeningSheet(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.getScreeningSheet(project, tailoring)
            .map(screeningSheet -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, screeningSheet))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Load screeningsheet file of tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet file loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Screeningsheet file does not exist",
            content = @Content)
    })
    @GetMapping(TAILORING_SCREENINGSHEET_PDF)
    @ResponseBody
    public ResponseEntity<byte[]> getScreeningSheetFile(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        return tailoringServiceRepository.getScreeningSheetFile(project, tailoring)
            .map(daten -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename("screeningsheet.pdf").build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(daten.length)
                .body(daten))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Load sectionvector applied to tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Applied selectionvector loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Selectioncvector does not exist",
            content = @Content)
    })
    @GetMapping(value = TAILORING_SELECTIONVECTOR, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<SelectionVectorResource>> getSelectionVector(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.getSelectionVector(project, tailoring)
            .map(selektionsVektor -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, selektionsVektor))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Load all tailoring data")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Tailoring loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.TAILORING, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringResource>> getTailoring(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringServiceRepository.getTailoring(project, tailoring)
            .map(loaded -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, loaded))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Add file to tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "File added to tailoring",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = Void.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @PostMapping(value = TAILORING_ATTACHMENTS, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<Void>> addFile(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "File to add") @RequestPart("datei") MultipartFile file) throws IOException {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.addFile(project, tailoring, file.getOriginalFilename(), file.getBytes()).isPresent() ?
            ResponseEntity.created(UriTemplate.of(ResourceMapper.TAILORINGREQUIRMENT).expand(pathContext.build().parameter())).build() :
            notFound().build();
//            .map(projektPhase -> ResponseEntity
//                .created(UriTemplate.of(ResourceMapper.TAILORINGREQUIRMENT).expand(pathContext.build().parameter()))
//                .created(linkTo(methodOn(TailoringController.class).createDokuments(project, tailoring)).toUri()))
//                .body(of(mapper.toResource(pathContext, projektPhase))))

//            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Generate all (tenant) documents of a specified tailoring.")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Documents created",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @GetMapping(TAILORING_DOCUMENT)
    @ResponseBody
    public ResponseEntity<byte[]> createDokuments(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        return tailoringService.createDocuments(project, tailoring)
            .map(dokument -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Generate tailoring requirement document")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "File created",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @GetMapping(TAILORING_DOCUMENT_CATALOG)
    @ResponseBody
    public ResponseEntity<byte[]> createRequirementFile(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        return tailoringService.createRequirementDocument(project, tailoring)
            .map(dokument -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Load all requirements of requested chpater")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Requirements of chapter loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringRequirementResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Chapter does not exist",
            content = @Content)
    })
    @GetMapping(TAILORING_CATALOG_CHAPTER_REQUIREMENT)
    @ResponseBody
    public ResponseEntity<CollectionModel<EntityModel<TailoringRequirementResource>>> getRequirements(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Chapter number") @PathVariable String chapter) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring)
            .chapter(chapter);

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(
                tailoringService.getRequirements(project, tailoring, chapter)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(domain -> of(mapper.toResource(pathContext, domain)))
                    .collect(toList())));
    }

    @Operation(summary = "Ermittlung aller für eine Projektphase definierten Zeichnungen für Anforderungsdokumente")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Dokumentzeichnungen wurden geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = DocumentSignatureResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Zeichnungen des Tailorings nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = TAILORING_SIGNATURE, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<DocumentSignatureResource>> getSigntures(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable String project,
        @Parameter(description = "Identifier des Tailorings") @PathVariable String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(
                tailoringService.getDocumentSignatures(project, tailoring)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(domain -> mapper.toResource(pathContext, domain))
                    .collect(toList())));
    }

    @Operation(summary = "Update signature of tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Signature updated",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = DocumentSignatureResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Signature does not exist",
            content = @Content)
    })
    @PutMapping(value = TAILORING_SIGNATURE_FACULTY, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<DocumentSignatureResource>> updateDokumentZeichnung(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Faculty of signature") @PathVariable String faculty,
        @Parameter(description = "Signature data to use") @RequestBody DocumentSignature signature) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.updateDocumentSignature(project, tailoring, signature)
            .map(zeichnung -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, zeichnung))))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Update name of tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Name updated",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = DocumentSignatureResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @PutMapping(value = ResourceMapper.TAILORING_NAME, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringResource>> updateName(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "New tailoring name") @RequestBody String name) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(name);

        return tailoringService.updateName(project, tailoring, name)
            .map(projektPhase -> ResponseEntity
                .ok()
                .body(of(mapper.toResource(pathContext, projektPhase))))
            .orElseThrow(() -> new ResourceException(PRECONDITION_FAILED, "Name could not be updated"));

    }

    @Operation(summary = "Load list of all attachment of tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "???",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = FileResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @GetMapping(value = TAILORING_ATTACHMENTS, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<FileResource>> getAttachmentList(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(
                tailoringServiceRepository.getFileList(project, tailoring)
                    .stream()
                    .map(domain -> mapper.toResource(pathContext, domain))
                    .collect(toList())));
    }

    @GetMapping(TAILORING_ATTACHMENT)
    @ResponseBody
    public ResponseEntity<byte[]> getAttachment(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Name of File") @PathVariable String name) {
        return tailoringServiceRepository.getFile(project, tailoring, name)
            .map(daten -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(name).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(daten.getType()))
                .contentLength(daten.getData().length)
                .body(daten.getData()))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Get file attached to tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "File loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = DocumentSignatureResource.class))),
        @ApiResponse(
            responseCode = "404", description = "File does not exist",
            content = @Content)
    })
    @DeleteMapping(TAILORING_ATTACHMENT)
    public ResponseEntity<Void> deleteAttachment(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Filename") @PathVariable("name") String name) {
        return tailoringServiceRepository.deleteFile(project, tailoring, name) ?
            ResponseEntity.ok().build() :
            notFound().build();
    }

    @Operation(summary = "Get document containg diffeences between automatic tailoring and current tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Comparsion document created",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @GetMapping(TAILORING_COMPARE)
    @ResponseBody
    public ResponseEntity<byte[]> getComparisonDocument(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        return tailoringService.createComparisonDocument(project, tailoring)
            .map(dokument -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder(MediaTypeProvider.FORM_DATA).name(MediaTypeProvider.ATTACHMENT).filename(dokument.getName()).build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(mediaTypeProvider.apply(dokument.getType()))
                .contentLength(dokument.getLength())
                .body(dokument.getData()))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Get all definded selectionvector profiles")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Profiles loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = SelectionVectorProfileResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Profiles do not exist",
            content = @Content)
    })
    @GetMapping(value = ResourceMapper.SELECTIONVECTOR_PROFILE, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<EntityModel<SelectionVectorProfileResource>>> getProfile() {
        PathContextBuilder pathContext = PathContext.builder();
        List<EntityModel<SelectionVectorProfileResource>> profile = tailoringServiceRepository.getSelectionVectorProfile()
            .stream()
            .map(profil -> of(mapper.toResource(pathContext, profil)))
            .collect(toList());

        return ResponseEntity
            .ok()
            .body(CollectionModel.of(profile));
    }

    @Operation(summary = "Update requirement state in accordance to provided file")
    @ApiResponse(responseCode = "202", description = "File evaluates")
    @PostMapping(ResourceMapper.TAILORING_REQUIREMENT_IMPORT)
    public ResponseEntity<EntityModel<Void>> updateAnforderungen(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @RequestPart("datei") MultipartFile datei) throws IOException {
        tailoringService.updateSelectedRequirements(project, tailoring, datei.getBytes());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Delete a tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Tailoring deleted",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = Void.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @DeleteMapping(ResourceMapper.TAILORING)
    public ResponseEntity<EntityModel<Void>> deleteTailoring(
        @Parameter(description = "fachlicher Projektschlüssel") @PathVariable String project,
        @Parameter(description = "Identifier des Tailorings") @PathVariable String tailoring) {
        Optional<Boolean> deleted = tailoringService.deleteTailoring(project, tailoring);
        return ResponseEntity.status(deleted.isPresent() ? OK : NOT_FOUND).build();
    }
}