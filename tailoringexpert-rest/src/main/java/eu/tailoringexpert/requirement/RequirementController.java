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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.TailoringRequirementResource;
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
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static eu.tailoringexpert.domain.ResourceMapper.CHAPTER_SELECTED;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORINGREQUIRMENT;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORINGREQUIRMENT_SELECTED;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORINGREQUIRMENT_TEXT;
import static org.springframework.http.ResponseEntity.notFound;

/**
 * REST-Controller for handling requirements.
 *
 * @author Michael Bädorf
 */
@Tag(name = "Requirement Controller", description = "Management of requirements")
@Log4j2
@RequiredArgsConstructor
@RestController
public class RequirementController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private RequirementService requirementService;

    @NonNull
    private RequirementServiceRepository requirementServiceRepository;


    @Operation(summary = "load Requirement")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Requirement geladen",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringRequirementResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Requirement nicht vorhanden",
            content = @Content)
    })
    @GetMapping(value = TAILORINGREQUIRMENT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringRequirementResource>> getRequirement(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Chapter number") @PathVariable String chapter,
        @Parameter(description = "Requirement position in chapter") @PathVariable String requirement) {
        log.traceEntry();

        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring)
            .chapter(chapter);
        ResponseEntity<EntityModel<TailoringRequirementResource>> result = requirementServiceRepository.getRequirement(project, tailoring, chapter, requirement)
            .map(projektPhaseAnforderung -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, projektPhaseAnforderung))))
            .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;
    }

    @Operation(summary = "Change selection state of all requirements in chapter and all subchapters")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "State changed"),
        @ApiResponse(
            responseCode = "404", description = "Requirement does not exist",
            content = @Content)
    })
    @PutMapping(value = TAILORINGREQUIRMENT_SELECTED, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringRequirementResource>> putRequirementState(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Chapter number") @PathVariable String chapter,
        @Parameter(description = "Requirement position in chapter") @PathVariable String requirement,
        @Parameter(description = "New selected state of requirement") @PathVariable Boolean selected) {
        log.traceEntry();

        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring)
            .chapter(chapter)
            .requirment(requirement);

        ResponseEntity<EntityModel<TailoringRequirementResource>> result = requirementService.handleSelected(project, tailoring, chapter, requirement, selected)
            .map(projektPhaseAnforderung -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, projektPhaseAnforderung))))
            .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;
    }

    @Operation(summary = "Change text of requirement")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Text changed"),
        @ApiResponse(
            responseCode = "404", description = "Requirement does not exist",
            content = @Content)
    })
    @PutMapping(value = TAILORINGREQUIRMENT_TEXT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringRequirementResource>> putRequirementText(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Chapter number") @PathVariable String chapter,
        @Parameter(description = "Requirement position in chapter") @PathVariable String requirement,
        @Parameter(description = "New requirement text") @RequestParam String text) {
        log.traceEntry();

        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring)
            .chapter(chapter)
            .requirment(requirement);

        ResponseEntity<EntityModel<TailoringRequirementResource>> result = requirementService.handleText(project, tailoring, chapter, requirement, text)
            .map(projektPhaseAnforderung -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, projektPhaseAnforderung))))
            .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;
    }

    @Operation(summary = "Change selection state of all requirements of chapter and all subchapters")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "State changed"),
        @ApiResponse(
            responseCode = "404", description = "Chapter does not exist",
            content = @Content)
    })
    @PutMapping(value = CHAPTER_SELECTED, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringCatalogChapterResource>> putRequirementsState(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Chapter number") @PathVariable String chapter,
        @Parameter(description = "New requirement selected state") @PathVariable Boolean selected) {
        log.traceEntry();

        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring)
            .chapter(chapter);

        ResponseEntity<EntityModel<TailoringCatalogChapterResource>> result = requirementService.handleSelected(project, tailoring, chapter, selected)
            .map(gruppe -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, gruppe))))
            .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;
    }

    @Operation(summary = "Add new requirement to tailoring")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Requirement added to tailoring",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = TailoringRequirementResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Tailoring does not exist",
            content = @Content)
    })
    @PostMapping(value = TAILORINGREQUIRMENT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<TailoringRequirementResource>> postRequirement(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Tailoring name") @PathVariable String tailoring,
        @Parameter(description = "Chapter number") @PathVariable String chapter,
        @Parameter(description = "Position of requirements after that the new requirement shall be inserted") @PathVariable String requirement,
        @Parameter(description = "Text of new requirement") @RequestParam String text) {
        log.traceEntry();

        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(tailoring)
            .chapter(chapter);
        ResponseEntity<EntityModel<TailoringRequirementResource>> result = requirementService.createRequirement(project, tailoring, chapter, requirement, text)
            .map(erstellteAnforderung -> ResponseEntity
                .created(UriTemplate.of(TAILORINGREQUIRMENT).expand(pathContext.build().parameter()))
                .body(EntityModel.of(mapper.toResource(pathContext, erstellteAnforderung))))
            .orElseGet(() -> notFound().build());

        log.traceExit();
        return result;
    }
}
