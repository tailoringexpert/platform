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
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.NoteResource;
import eu.tailoringexpert.domain.SelectionVectorProfileResource;
import eu.tailoringexpert.domain.Tailoring;
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
import eu.tailoringexpert.domain.TailoringState;
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
import org.springframework.http.HttpStatus;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static eu.tailoringexpert.domain.ResourceMapper.TAILORING;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_ATTACHMENTS;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_ATTACHMENT;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_CATALOG;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_CATALOG_CHAPTER;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_CATALOG_CHAPTER_REQUIREMENT;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_COMPARE;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_DOCUMENT;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_DOCUMENT_CATALOG;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_NAME;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_NOTE;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_NOTES;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_REQUIREMENT_IMPORT;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SCREENINGSHEET;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SCREENINGSHEET_PDF;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SELECTIONVECTOR;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SIGNATURE;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_SIGNATURE_FACULTY;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING_STATE;
import static org.springframework.hateoas.EntityModel.of;
import static org.springframework.hateoas.server.mvc.BasicLinkBuilder.linkToCurrentMapping;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;


/**
 * REST-Controller for management of tailorings.
 *
 * @author Michael Bädorf
 */
@Tag(name = "Tailoring Controller", description = "Management of tailorings")
@RequiredArgsConstructor
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
            .map(serviceResult -> ok()
                .body(of(mapper.toResource(pathContext, serviceResult))))
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
            .map(k -> ok()
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
            .map(screeningSheet -> ok()
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
            .map(daten -> ok()
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
            .map(selektionsVektor -> ok()
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
    @GetMapping(value = TAILORING, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringResource>> getTailoring(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringServiceRepository.getTailoring(project, tailoring)
            .map(loaded -> ok()
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
    public ResponseEntity<EntityModel<Void>> postFile(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "File to add") @RequestPart("datei") MultipartFile file) throws IOException {
        Optional<Tailoring> serviceResult = tailoringService.addFile(project, tailoring, file.getOriginalFilename(), file.getBytes());

        if (serviceResult.isEmpty()) {
            return notFound().build();
        }

        Map<String, String> parameters = PathContext.builder()
            .project(project)
            .tailoring(tailoring)
            .build()
            .parameter();
        parameters.put("name", file.getOriginalFilename());
        return created(UriTemplate.of(linkToCurrentMapping().toString() + "/" + TAILORING_ATTACHMENT).expand(parameters))
            .build();
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
    public ResponseEntity<byte[]> getDocuments(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        return tailoringService.createDocuments(project, tailoring)
            .map(dokument -> ok()
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
    public ResponseEntity<byte[]> getRequirementFile(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        return tailoringService.createRequirementDocument(project, tailoring)
            .map(dokument -> ok()
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

        return ok()
            .body(CollectionModel.of(
                tailoringService.getRequirements(project, tailoring, chapter)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(domain -> of(mapper.toResource(pathContext, domain)))
                    .toList()));
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

        return ok()
            .body(CollectionModel.of(
                tailoringService.getDocumentSignatures(project, tailoring)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(domain -> mapper.toResource(pathContext, domain))
                    .toList()));
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
    public ResponseEntity<EntityModel<DocumentSignatureResource>> updateDocumentSignature(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Faculty of signature") @PathVariable String faculty,
        @Parameter(description = "Signature data to use") @RequestBody DocumentSignature signature) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.updateDocumentSignature(project, tailoring, signature)
            .map(zeichnung -> ok()
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
    @PutMapping(value = TAILORING_NAME, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringResource>> putName(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "New tailoring name") @RequestBody String name) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(name);

        return tailoringService.updateName(project, tailoring, name)
            .map(projektPhase -> ok()
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

        return tailoringServiceRepository.getFileList(project, tailoring)
            .map(data -> ok()
                .body(CollectionModel.of(
                    data.stream()
                        .map(domain -> mapper.toResource(pathContext, domain))
                        .toList()
                ))
            )
            .orElseGet(() -> notFound().build());
    }

    @GetMapping(TAILORING_ATTACHMENT)
    @ResponseBody
    public ResponseEntity<byte[]> getAttachment(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Name of File") @PathVariable String name) {
        return tailoringServiceRepository.getFile(project, tailoring, name)
            .map(daten -> ok()
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
            ok().build() :
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
            .map(dokument -> ok()
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
    public ResponseEntity<CollectionModel<EntityModel<SelectionVectorProfileResource>>> getProfiles() {
        PathContextBuilder pathContext = PathContext.builder();
        List<EntityModel<SelectionVectorProfileResource>> profile = tailoringServiceRepository.getSelectionVectorProfile()
            .stream()
            .map(profil -> of(mapper.toResource(pathContext, profil)))
            .toList();

        return ok()
            .body(CollectionModel.of(profile));
    }

    @Operation(summary = "Update requirement state in accordance to provided file")
    @ApiResponse(responseCode = "202", description = "File evaluates")
    @PostMapping(TAILORING_REQUIREMENT_IMPORT)
    public ResponseEntity<EntityModel<Void>> postRequirements(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @RequestPart("datei") MultipartFile datei) throws IOException {
        tailoringService.updateImportedRequirements(project, tailoring, datei.getBytes());
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
    @DeleteMapping(TAILORING)
    public ResponseEntity<EntityModel<Void>> deleteTailoring(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        Optional<Boolean> deleted = tailoringService.deleteTailoring(project, tailoring);

        HttpStatus result = NOT_FOUND;
        if (deleted.isPresent()) {
            result = deleted.get().booleanValue() ? OK : PRECONDITION_FAILED;
        }

        return ResponseEntity.status(result).build();
    }

    @Operation(summary = "Load notes of a tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Tailoring loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = NoteResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @GetMapping(value = TAILORING_NOTES, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<NoteResource>> getNotes(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.getNotes(project, tailoring)
            .map(data -> ok()
                .body(CollectionModel.of(
                    data.stream()
                        .map(domain -> mapper.toResource(pathContext, domain))
                        .toList()
                ))
            )
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Load note of a tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Tailoring loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = NoteResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Note does not exist",
            content = @Content)
    })
    @GetMapping(value = TAILORING_NOTE, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<NoteResource>> getNote(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Number of note") @PathVariable Integer note) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring)
            .note(note.toString());

        return tailoringService.getNote(project, tailoring, note)
            .map(loaded -> ok()
                .body(of(mapper.toResource(pathContext, loaded))))
            .orElseGet(() -> notFound().build());

    }

    @Operation(summary = "Add a new note to tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Note added",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = Void.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @PostMapping(TAILORING_NOTES)
    public ResponseEntity<EntityModel<Void>> postNote(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Text of note to add") @RequestBody String note) {
        Optional<Note> addedNote = tailoringService.addNote(project, tailoring, note);
        if (addedNote.isEmpty()) {
            return notFound().build();
        }

        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return created(mapper.createLink(ResourceMapper.REL_SELF, linkToCurrentMapping().toString(),
                TAILORING_NOTE,
                pathContext.note(addedNote.get().getNumber().toString()).build().parameter())
            .toUri())
            .build();
    }

    @Operation(summary = "Set state of tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "State changed",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @PutMapping(TAILORING_STATE)
    public ResponseEntity<EntityModel<TailoringResource>> putState(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "State to set") @PathVariable TailoringState state) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring);

        return tailoringService.updateState(project, tailoring, state)
            .map(updatedTailoring -> ok()
                .body(of(mapper.toResource(pathContext, updatedTailoring))))
            .orElseGet(() -> notFound().build());
    }
}
