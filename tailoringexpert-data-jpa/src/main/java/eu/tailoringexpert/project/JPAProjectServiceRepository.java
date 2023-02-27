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
import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.Project;
import eu.tailoringexpert.domain.ProjectEntity;
import eu.tailoringexpert.domain.ProjectInformation;
import eu.tailoringexpert.domain.ProjectState;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;

import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link ProjectServiceRepository}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
@Transactional
public class JPAProjectServiceRepository implements ProjectServiceRepository {

    public static final String CACHE_BASECATALOG = "ProjectServiceRepository#BaseCatalog";

    @NonNull
    private JPAProjectServiceRepositoryMapper mapper;

    @NonNull
    private ProjectRepository projectRepository;

    @NonNull
    private BaseCatalogRepository baseCatalogRepository;

    /**
     * {@inheritDoc}
     */
    @Cacheable(CACHE_BASECATALOG)
    @Override
    public Catalog<BaseRequirement> getBaseCatalog(String version) {
        log.traceEntry(() -> version);

        BaseCatalogEntity entity = baseCatalogRepository.findByVersion(version);
        Catalog<BaseRequirement> result = mapper.toDomain(entity);

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Project createProject(Project project) {
        log.traceEntry(project::getIdentifier);

        ProjectEntity toSave = mapper.createProject(project);
        toSave = projectRepository.save(toSave);
        Project result = mapper.toDomain(toSave);

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProject(String project) {
        log.traceEntry(() -> project);
        Long deletedProjekte = projectRepository.deleteByIdentifier(project);
        return log.traceExit(deletedProjekte.intValue() > 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Project> getProject(String project) {
        log.traceEntry(() -> project);
        Optional<Project> result = ofNullable(mapper.toDomain(projectRepository.findByIdentifier(project)));
        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> addTailoring(String project, Tailoring tailoring) {
        log.traceEntry(() -> project, tailoring::getName);

        ProjectEntity eProject = projectRepository.findByIdentifier(project);
        TailoringEntity eTailoring = mapper.toEntity(tailoring);

        eProject.getTailorings().add(eTailoring);

        projectRepository.flush();
        Optional<Tailoring> result = ofNullable(mapper.toDomain(eTailoring));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ProjectInformation> getProjectInformations() {
        log.traceEntry();

        List<ProjectInformation> result = projectRepository.findAll()
            .stream()
            .map(mapper::getProjectInformationen)
            .toList();

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProjectInformation> getProjectInformation(String project) {
        log.traceEntry(() -> project);
        Optional<ProjectInformation> result = ofNullable(mapper.getProjectInformationen(projectRepository.findByIdentifier(project)));
        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> getScreeningSheetFile(String project) {
        log.traceEntry(() -> project);

        ProjectEntity entity = projectRepository.findByIdentifier(project);
        if (isNull(entity)) {
            log.traceExit();
            return empty();
        }

        Optional<byte[]> result = Optional.of(entity.getScreeningSheet().getData());

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(String project) {
        log.traceEntry(() -> project);

        ProjectEntity entity = projectRepository.findByIdentifier(project);
        if (isNull(entity)) {
            log.traceExit();
            return empty();
        }

        Optional<ScreeningSheet> result = ofNullable(mapper.getScreeningSheet(entity.getScreeningSheet()));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProjectInformation> updateState(String project, ProjectState state) {
        log.traceEntry(() -> project, () -> state);

        ProjectEntity entity = projectRepository.findByIdentifier(project);
        if (isNull(entity)) {
            log.traceExit();
            return empty();
        }

        entity.setState(state);
        Optional<ProjectInformation> result = ofNullable(mapper.getProjectInformationen(entity));

        log.traceExit();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExistingProject(String project) {
        log.traceEntry(() -> project);
        return log.traceExit(projectRepository.existsProjectByIdentifier(project));
    }
}
