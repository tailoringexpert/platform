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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Note;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import eu.tailoringexpert.tailoring.TailoringService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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
        Catalog<BaseRequirement> catalog = repository.getBaseCatalog(catalogVersion);
        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);

        Tailoring tailoring = tailoringService.createTailoring("master", "1000", screeningSheet, applicableSelectionVector, note, catalog);

        Project project = repository.createProject(Project.builder()
            .screeningSheet(screeningSheet)
            .identifier(screeningSheet.getProject())
            .tailoring(tailoring)
            .build()
        );

        log.info("Project {} with phases {} created with catalog {}", screeningSheet.getProject(), tailoring.getPhases(), catalogVersion);
        return CreateProjectTO.builder()
            .project(project.getIdentifier())
            .tailoring(tailoring.getName())
            .selectionVector(applicableSelectionVector)
            .build();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProject(String project) {
        Optional<Project> toDelete = repository.getProject(project);
        if (toDelete.isPresent()) {
            return repository.deleteProject(project);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> addTailoring(String project, String catalog, byte[] screeningSheetData, SelectionVector applicableSelectionVector, String note) {
        log.info("STARTED  | adding tailoring to project {}", project);
        Optional<Project> oProject = repository.getProject(project);
        if (oProject.isEmpty()) {
            return empty();
        }

        Catalog<BaseRequirement> baseCatalog = repository.getBaseCatalog(catalog);
        if (isNull(baseCatalog)) {
            log.error("ABORTED  | catalog {} does not exist", catalog);
            return empty();
        }

        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);
        // nur hinzufüegen, wenn "richtiges" Project
        if (!project.equals(screeningSheet.getProject())) {
            log.error("ABORTED  | screeningsheet defines phase of project {} instead of {}", screeningSheet.getProject(), project);
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
        log.info("FINISHED | adding phase {} to project {}", tailoringName, project);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Project> copyProject(String project, byte[] screeningSheetData) {
        log.info("STARTED  | copyig project {}", project);
        Optional<Project> projectToCopy = repository.getProject(project);
        if (projectToCopy.isEmpty()) {
            log.info("Project does not exist. Aborting");
            return empty();
        }

        // annahme:
        // 1. alle phasen werden kopiert und das projekt screeningsheet gesetzt
        // 2. als screeningsheet pro phase wird das projekt screeningsheet gespeichert
        // 3. selektionvektor der phase wird aus kopierten projekt übernommen (zur info)
        // 4. KEIN neutailoring!

        Project projectCopy = projectToCopy.get();

        ScreeningSheet screeningSheet = screeningSheetService.createScreeningSheet(screeningSheetData);
        projectCopy.setScreeningSheet(screeningSheet);
        projectCopy.setIdentifier(screeningSheet.getProject());

        projectCopy.getTailorings()
            .forEach(tailorings -> {
                log.debug("Copying tailoring {}", tailorings.getName());
                tailorings.setScreeningSheet(screeningSheet);
            });

        Optional<Project> result = of(repository.createProject(projectCopy));
        log.info("FINISHED | project {} copied to {}", project, screeningSheet.getProject());
        return result;
    }
}
