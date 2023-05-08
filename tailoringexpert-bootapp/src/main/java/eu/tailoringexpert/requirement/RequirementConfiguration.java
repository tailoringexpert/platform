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

import eu.tailoringexpert.Tenant;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import lombok.NonNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class RequirementConfiguration {

    @Bean
    JPARequirementServiceRepositoryMapper jpaRequirementServiceRepositoryMapper(
        @NonNull LogoRepository logoRepository,
        @NonNull DRDRepository drdRepository) {
        JPARequirementServiceRepositoryMapperGenerated result = new JPARequirementServiceRepositoryMapperGenerated();
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
    RequirementModifiablePredicateRepository requirementModifiablePredicateRepository(
        @NonNull ProjectRepository projectRepository) {
        return new JPARequirementModifiablePredicateRepository(projectRepository);
    }

    @Bean
    DefaultRequirementModifiablePredicate defaultRequirementModifiablePredicate(
        @NonNull RequirementModifiablePredicateRepository repository) {
        return new DefaultRequirementModifiablePredicate(repository);
    }

    @Bean
    RequirementService requirementService(
        @NonNull RequirementServiceRepository repository,
        @NonNull DefaultRequirementModifiablePredicate predicate) {
        return new RequirementServiceImpl(repository, predicate);
    }

    @Bean
    RequirementController requirementController(
        @NonNull ResourceMapper mapper,
        @NonNull RequirementService requirementService,
        @NonNull RequirementServiceRepository requirementServiceRepository) {
        return new RequirementController(mapper, requirementService, requirementServiceRepository);
    }

    @Bean
    @Primary
    RequirementModifiablePredicate requirementModifiablePredicate(
        @NonNull DefaultRequirementModifiablePredicate defaultRequirementModifiablePredicate,
        @NonNull ListableBeanFactory beanFactory) {
        Map<String, RequirementModifiablePredicate> predicates = getTenantImplementierungen(beanFactory, RequirementModifiablePredicate.class);
        return new TenantRequirementModifiablePredicate(predicates, defaultRequirementModifiablePredicate);
    }

    private <T> Map<String, T> getTenantImplementierungen(ListableBeanFactory beanFactory, Class<T> clz) {
        return beanFactory.getBeansOfType(clz)
            .values()
            .stream()
            // Defaultimplementation has no tenant annotation!
            .filter(bean -> Objects.nonNull(bean.getClass().getAnnotation(Tenant.class)))
            .collect(Collectors.toMap(bean -> bean.getClass().getAnnotation(Tenant.class).value(), Function.identity()));
    }
}
