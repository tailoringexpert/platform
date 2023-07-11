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

import eu.tailoringexpert.TailoringexpertException;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectInformation;
import eu.tailoringexpert.domain.ProjectState;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import eu.tailoringexpert.tailoring.TailoringService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;

import static eu.tailoringexpert.domain.ProjectState.ONGOING;
import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Implementation of {@link ProjectService}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    @NonNull
    private ProjectServiceRepository repository;

    @NonNull
    private ScreeningSheetService screeningSheetService;

    @NonNull
    private TailoringService tailoringService;

    /**
     * {@inheritDoc}
     */
    @Override
    public CreateProjectTO createProject(String catalogVersion, byte[] screeningSheetData, SelectionVector applicableSelectionVector, String note) {
        log.traceEntry(() -> catalogVersion);

        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);
        String identifier = screeningSheet.getProject();
        if (repository.isExistingProject(identifier)) {
            throw new TailoringexpertException("A project with name " + identifier + " already exists!\nEither change project identifier or add new tailoring to exitsing project " + identifier);
        }

        Catalog<BaseRequirement> catalog = repository.getBaseCatalog(catalogVersion);
        Tailoring tailoring = tailoringService.createTailoring("master", "1000", screeningSheet, applicableSelectionVector, note, catalog);

        Project project = repository.createProject(Project.builder()
            .screeningSheet(screeningSheet)
            .identifier(identifier)
            .tailoring(tailoring)
            .state(ONGOING)
            .build()
        );

        return log.traceExit(CreateProjectTO.builder()
            .project(project.getIdentifier())
            .tailoring(tailoring.getName())
            .selectionVector(applicableSelectionVector)
            .build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProject(String project) {
        log.traceEntry(() -> project);

        Optional<Project> toDelete = repository.getProject(project);
        if (toDelete.isPresent()) {
            return log.traceExit(repository.deleteProject(project));
        }

        return log.traceExit(false);
    }

    @Override
    public Optional<ProjectInformation> updateState(String project, ProjectState state) {
        log.traceEntry(() -> project, () -> state);

        Optional<Project> oProject = repository.getProject(project);
        if (oProject.isEmpty()) {
            log.error("updating state of {} skipped because it does not exists", project);
            return log.traceExit(empty());
        }

        Optional<ProjectInformation> result = repository.updateState(project, state);
        return log.traceExit(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> addTailoring(String project, String catalog, byte[] screeningSheetData, SelectionVector applicableSelectionVector, String note) {
        log.traceEntry(() -> project, () -> catalog);

        Optional<Project> oProject = repository.getProject(project);
        if (oProject.isEmpty()) {
            log.traceExit(false);
            return empty();
        }

        Catalog<BaseRequirement> baseCatalog = repository.getBaseCatalog(catalog);
        if (isNull(baseCatalog)) {
            log.error("catalog {} does not exist", catalog);
            log.traceExit();
            return empty();
        }

        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);
        // nur hinzufüegen, wenn "richtiges" Project
        if (!project.equals(screeningSheet.getProject())) {
            log.error("screeningsheet defines phase of project {} instead of {}", screeningSheet.getProject(), project);
            log.traceExit();
            return empty();
        }

        Project addTo = oProject.get();
        StringBuilder tailoringName = new StringBuilder("master");
        if (!addTo.getTailorings().isEmpty()) {
            tailoringName.append(addTo.getTailorings().size());
        }

        Optional<String> identifier = oProject.get()
            .getTailorings()
            .stream()
            .map(p -> parseInt(p.getIdentifier()))
            .max(comparingInt(Integer::intValue))
            .map(max -> String.valueOf(max + 1));

        Tailoring tailoring = tailoringService.createTailoring(
            tailoringName.toString(),
            identifier.orElse("1000"),
            screeningSheet,
            applicableSelectionVector,
            note,
            baseCatalog
        );

        Optional<Tailoring> result = repository.addTailoring(project, tailoring);
        log.traceExit(result.isPresent());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Project> copyProject(String project, byte[] screeningSheetData) {
        log.traceEntry(() -> project);

        Optional<Project> projectToCopy = repository.getProject(project);
        if (projectToCopy.isEmpty()) {
            log.error("Project does not exist. Aborting");
            log.traceExit();
            return empty();
        }

        // annahme:
        // 1. all phases will be copied, screeningsheet of new project will be set
        // 2. selectionvector of copied tailoring wille also be copied
        // 3. no tailoring updated

        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);
        if (repository.isExistingProject(screeningSheet.getProject())) {
            log.error("Exception while copying project because a project with same name already exists");
            log.traceExit();
            throw new TailoringexpertException("A project with name " + project + " already exists!\nEither change project identifier or add new tailoring to existing project.");
        }

        Project projectCopy = projectToCopy.get();
        projectCopy.setScreeningSheet(screeningSheet);
        projectCopy.setIdentifier(screeningSheet.getProject());

        projectCopy.getTailorings()
            .forEach(tailorings -> {
                log.debug("Copying tailoring {}", tailorings.getName());
                tailorings.setScreeningSheet(screeningSheet);
            });

        Optional<Project> result = of(repository.createProject(projectCopy));
        log.traceExit(result.isPresent());
        return result;
    }
}
