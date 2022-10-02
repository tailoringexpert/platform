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

import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequirementConfiguration {

    @Bean
    JPARequirementServiceRepositoryMapper jpaRequirementServiceRepositoryMapper(
        @NonNull LogoRepository logoRepository,
        @NonNull DRDRepository drdRepository) {
        JPARequirementServiceRepositoryMapperImpl result = new JPARequirementServiceRepositoryMapperImpl();
        result.setLogoRepository(logoRepository);
        result.setDrdRepository(drdRepository);
        return result;
    }

    @Bean
    RequirementServiceRepository requirementServiceRepository(
        @NonNull JPARequirementServiceRepositoryMapper mapper,
        @NonNull ProjectRepository projectRepository) {
        return new JPARequirementServiceRepository(mapper, projectRepository);
    }

    @Bean
    RequirementService requirementService(
        @NonNull RequirementServiceRepository repository) {
        return new RequirementServiceImpl(repository);
    }

    @Bean
    RequirementController requirementController(
        @NonNull ResourceMapper mapper,
        @NonNull RequirementService requirementService,
        @NonNull RequirementServiceRepository requirementServiceRepository) {
        return new RequirementController(mapper, requirementService, requirementServiceRepository);
    }
}
