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

import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import eu.tailoringexpert.tailoring.TailoringService;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectConfiguration {

    @Bean
    JPAProjectServiceRepositoryMapper jpaProjektServiceRepositoryMapper(
        @NonNull BaseCatalogRepository baseCatalogRepository,
        @NonNull LogoRepository logoRepository,
        @NonNull DRDRepository drdRepository) {
        JPAProjectServiceRepositoryMapperGenerated result = new JPAProjectServiceRepositoryMapperGenerated();
        result.setBaseCatalogRepository(baseCatalogRepository);
        result.setLogoRepository(logoRepository);
        result.setDrdRepository(drdRepository);
        return result;
    }

    @Bean
    ProjectServiceRepository projectServiceRepository(
        @NonNull JPAProjectServiceRepositoryMapper mapper,
        @NonNull ProjectRepository projectRepository,
        @NonNull BaseCatalogRepository baseCatalogRepository) {
        return new JPAProjectServiceRepository(mapper, projectRepository, baseCatalogRepository);
    }

    @Bean
    ProjectService projektService(@NonNull ProjectServiceRepository repository,
                                  @NonNull ScreeningSheetService screeningSheetService,
                                  @NonNull TailoringService tailoringService) {
        return new ProjectServiceImpl(repository, screeningSheetService, tailoringService);
    }

    @Bean
    ProjectController projektController(
        @NonNull ResourceMapper mapper,
        @NonNull ProjectService projectService,
        @NonNull ProjectServiceRepository projectServiceRepository) {
        return new ProjectController(mapper, projectService, projectServiceRepository);
    }

}
