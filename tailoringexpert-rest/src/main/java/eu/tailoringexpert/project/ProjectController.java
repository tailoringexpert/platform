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
package eu.tailoringexpert.project;

import eu.tailoringexpert.domain.PathContext;
import eu.tailoringexpert.domain.PathContext.PathContextBuilder;
import eu.tailoringexpert.domain.ProjectInformation;
import eu.tailoringexpert.domain.ProjectResource;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.ScreeningSheetResource;
import eu.tailoringexpert.domain.SelectionVectorResource;
import eu.tailoringexpert.domain.Tailoring;
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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static eu.tailoringexpert.domain.ResourceMapper.PROJECT;
import static eu.tailoringexpert.domain.ResourceMapper.PROJECTS;
import static eu.tailoringexpert.domain.ResourceMapper.PROJECT_NEW;
import static eu.tailoringexpert.domain.ResourceMapper.PROJECT_SCREENINGSHEET;
import static eu.tailoringexpert.domain.ResourceMapper.PROJECT_SCREENINGSHEET_PDF;
import static eu.tailoringexpert.domain.ResourceMapper.PROJECT_SELECTIONVECTOR;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORING;
import static eu.tailoringexpert.domain.ResourceMapper.TAILORINGS;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.BasicLinkBuilder.linkToCurrentMapping;
import static org.springframework.http.HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.ResponseEntity.notFound;

/**
 * REST-Controller for management of projects.
 *
 * @author Michael Bädorf
 */
@Tag(name = "Project Controller", description = "Management of projects")
@Log4j2
@AllArgsConstructor
@RestController
public class ProjectController {

    @NonNull
    private ResourceMapper mapper;

    @NonNull
    private ProjectService projectService;

    @NonNull
    private ProjectServiceRepository projectServiceRepository;

    @Operation(summary = "Load all projects base data")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Project list created",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProjectResource.class))))
    })
    @GetMapping(value = PROJECTS, produces = {"application/hal+json"})
    public ResponseEntity<CollectionModel<EntityModel<ProjectResource>>> getProjects() {
        List<EntityModel<ProjectResource>> projekte = projectServiceRepository.getProjectInformations()
            .stream()
            .map(domain -> EntityModel.of(mapper.toResource(PathContext.builder(), domain)))
            .collect(toList());
        return ResponseEntity
            .ok()
            .body(CollectionModel.of(projekte));

    }

    @Operation(summary = "Create new project")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Project created",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = Void.class)))
    })
    @PostMapping(value = PROJECT_NEW, produces = {"application/hal+json"})
    public ResponseEntity<Void> postProject(
        @Parameter(description = "Base catalog for project creation") @PathVariable String version,
        @Parameter(description = "New project configuration data") @RequestBody ProjectCreationRequest request) {
        CreateProjectTO project = projectService.createProject(version, request.getScreeningSheet().getData(), request.getSelectionVector(), request.getNote());

        return ResponseEntity
            .created(mapper.createLink(ResourceMapper.REL_SELF, linkToCurrentMapping().toString(), PROJECT, Map.of("project", project.getProject())).toUri())
            .build();
    }

    @Operation(summary = "Load requested project")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Project loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = ProjectResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Project does not exist")
    })
    @GetMapping(value = PROJECT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<ProjectResource>> getProject(
        @Parameter(description = "Project identifier") @PathVariable String project) {

        PathContextBuilder pathContextBuilder = PathContext.builder();
        Optional<ProjectInformation> result = projectServiceRepository.getProjectInformation(project);

        return result.map(pi -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContextBuilder, pi))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Load screeningsheet values of requested project")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet of project loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = ScreeningSheetResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Screeningsheet could not be loaded")
    })
    @GetMapping(value = PROJECT_SCREENINGSHEET, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<ScreeningSheetResource>> getScreeningSheet(
        @Parameter(description = "Project identifier") @PathVariable String project) {

        PathContextBuilder pathContext = PathContext.builder()
            .project(project);
        return projectServiceRepository.getScreeningSheet(project)
            .map(screeningSheet -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, screeningSheet))))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Load screeningsheet file of requested project")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Screeningsheet file loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = byte[].class)))
    })
    @GetMapping(PROJECT_SCREENINGSHEET_PDF)
    @ResponseBody
    public ResponseEntity<byte[]> getScreeningSheetFile(
        @Parameter(description = "Project identifier") @PathVariable String project) {

        return projectServiceRepository.getScreeningSheetFile(project)
            .map(daten -> ResponseEntity
                .ok()
                .header(CONTENT_DISPOSITION, ContentDisposition.builder("form-data").name("attachment").filename("screeningsheet.pdf").build().toString())
                .header(ACCESS_CONTROL_EXPOSE_HEADERS, CONTENT_DISPOSITION)
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(daten.length)
                .body(daten))
            .orElseGet(() -> notFound().build());
    }

    @Operation(summary = "Delete project")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", description = "Project deleted",
            content = @Content(schema = @Schema(implementation = Void.class))),
        @ApiResponse(
            responseCode = "400", description = "Project not deleted",
            content = @Content)
    })
    @DeleteMapping(value = PROJECT, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<Void>> deleteProject(
        @Parameter(description = "Project identifier") @PathVariable String project) {

        boolean deleted = projectService.deleteProject(project);
        return ResponseEntity.status(deleted ? NO_CONTENT : BAD_REQUEST).build();
    }

    @Operation(summary = "Create project copy")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Project copy created",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = ProjectResource.class))),
        @ApiResponse(
            responseCode = "404", description = "Project not copied",
            content = @Content)
    })
    @PostMapping(value = PROJECT, produces = {"application/hal+json"})
    @ResponseBody
    public ResponseEntity<EntityModel<ProjectResource>> copyProject(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @Parameter(description = "Raw data of screeningsheet") @RequestPart("datei") MultipartFile screeningSheet) throws IOException {

        return projectService.copyProject(project, screeningSheet.getBytes())
            .map(projektKopie -> ResponseEntity
                .created(mapper.createLink(ResourceMapper.REL_SELF, linkToCurrentMapping().toString(), PROJECT, Map.of("project", projektKopie.getIdentifier())).toUri())
                .body(EntityModel.of(mapper.toResource(PathContext.builder(), projektKopie))))
            .orElseGet(() -> notFound().build());
    }


    @Operation(summary = "Add tailoring to project")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", description = "Tailoring created and added to project",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = Void.class)))
    })
    @PostMapping(value = TAILORINGS, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<Void>> postTailoring(
        @Parameter(description = "Project identifier") @PathVariable String project,
        @RequestBody ProjectCreationRequest request) {

        Optional<Tailoring> result = projectService.addTailoring(project, request.getCatalog(), request.getScreeningSheet().getData(), request.getSelectionVector(), request.getNote());
        if (result.isEmpty()) {
            return notFound()
                .build();
        }

        PathContextBuilder pathContext = PathContext.builder()
            .project(project)
            .tailoring(result.get().getName());

        return ResponseEntity
            .created(mapper.createLink(ResourceMapper.REL_SELF, linkToCurrentMapping().toString(),
                    TAILORING,
                    pathContext.build().parameter())
                .toUri())
            .build();
    }

    @Operation(summary = "Load selection vector of project")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", description = "Selection vector loaded",
            content = @Content(mediaType = "application/json+hal", schema = @Schema(implementation = SelectionVectorResource.class))),
        @ApiResponse(
            responseCode = "404", description = "selection vector not loaded",
            content = @Content)
    })
    @GetMapping(value = PROJECT_SELECTIONVECTOR, produces = {"application/hal+json"})
    public ResponseEntity<EntityModel<SelectionVectorResource>> getSelectionVector(
        @Parameter(description = "fachlicher Projektschlüssel")
        @PathVariable("project") String project) {
        PathContextBuilder pathContext = PathContext.builder()
            .project(project);
        return projectServiceRepository.getProject(project)
            .map(p -> ResponseEntity
                .ok()
                .body(EntityModel.of(mapper.toResource(pathContext, p.getScreeningSheet().getSelectionVector()))))
            .orElseGet(() -> notFound().build());
    }

}
