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
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringEntity;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Transactional
public class JPAProjectServiceRepository implements ProjektServiceRepository {

    public static final String CACHE_KATALOG = "ProjectServiceRepository#Catalog";

    @NonNull
    private JPAProjectServiceRepositoryMapper mapper;

    @NonNull
    private ProjectRepository projectRepository;

    @NonNull
    private BaseCatalogRepository baseCatalogRepository;

    /**
     * {@inheritDoc}
     */
    @Cacheable(CACHE_KATALOG)
    @Override
    public Catalog<BaseRequirement> getBaseCatalog(String version) {
        return mapper.toDomain(getKatalogDefinition(version));
    }

    private BaseCatalogEntity getKatalogDefinition(String version) {
        return baseCatalogRepository.findByVersion(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Project createProject(String catalog, Project project) {
        ProjectEntity entity = projectRepository.save(mapper.createProject(project));

        BaseCatalogEntity katalogDefinition = baseCatalogRepository.findByVersion(catalog);
        entity.getTailorings().get(0).setBaseCatalog(katalogDefinition);

        return mapper.toDomain(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Project createProject(Project project) {
        ProjectEntity result = mapper.createProject(project);
        result = projectRepository.save(result);
        return mapper.toDomain(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteProjekt(String project) {
        Long deletedProjekte = projectRepository.deleteByIdentifier(project);
        return deletedProjekte.intValue() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Project> getProject(String project) {
        return ofNullable(mapper.toDomain(projectRepository.findByIdentifier(project)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Tailoring> addTailoring(String project, Tailoring tailoring) {
        ProjectEntity projekt = projectRepository.findByIdentifier(project);
        TailoringEntity eProjektPhase = mapper.toEntity(tailoring);

        BaseCatalogEntity katalogDefinition = baseCatalogRepository.findByVersion(tailoring.getCatalog().getVersion());
        eProjektPhase.setBaseCatalog(katalogDefinition);

        projekt.setTailorings(isNull(projekt.getTailorings()) ? new ArrayList<>() : new ArrayList<>(projekt.getTailorings()));
        projekt.getTailorings().add(eProjektPhase);

        projectRepository.flush();
        return ofNullable(mapper.toDomain(eProjektPhase));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ProjectInformation> getProjectInformations() {
        return projectRepository.findAll()
            .stream()
            .map(mapper::getTailoringInformationen)
            .collect(toList());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ProjectInformation> getProjectInformation(String project) {
        return ofNullable(mapper.getTailoringInformationen(projectRepository.findByIdentifier(project)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<byte[]> getScreeningSheetFile(String project) {
        ProjectEntity entity = projectRepository.findByIdentifier(project);
        if (isNull(entity)) {
            return empty();
        }

        return Optional.of(entity.getScreeningSheet().getData());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<ScreeningSheet> getScreeningSheet(String project) {
        ProjectEntity entity = projectRepository.findByIdentifier(project);
        if (isNull(entity)) {
            return empty();
        }

        return ofNullable(mapper.getScreeningSheet(entity.getScreeningSheet()));
    }


}
