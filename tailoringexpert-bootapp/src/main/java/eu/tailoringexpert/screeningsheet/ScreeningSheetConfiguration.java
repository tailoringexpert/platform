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
package eu.tailoringexpert.screeningsheet;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.Tenant;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.ParameterRepository;
import lombok.NonNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class ScreeningSheetConfiguration {

    @Bean
    JPAScreeningSheetServiceRepositoryMapper screeningSheetServiceRepositoryMapper(
        @NonNull ObjectMapper objectMapper) {
        JPAScreeningSheetServiceRepositoryMapperImpl result = new JPAScreeningSheetServiceRepositoryMapperImpl();
        result.setMapper(objectMapper);
        return result;
    }

    @Bean
    @Primary
    SelectionVectorProvider selectionVectorProvider(@NonNull ListableBeanFactory beanFactory) {
        Map<String, SelectionVectorProvider> services = getTenantImplementierungen(beanFactory, SelectionVectorProvider.class);
        return new TenantSelectionVectorProvider(services);
    }

    @Bean
    @Primary
    ScreeningSheetParameterProvider screeningSheetParameterProvider(@NonNull ListableBeanFactory beanFactory) {
        Map<String, ScreeningSheetParameterProvider> services = getTenantImplementierungen(beanFactory, ScreeningSheetParameterProvider.class);
        return new TenantScreeningSheetParameterProvider(services);
    }

    @Bean
    ScreeningSheetServiceRepository screeningSheetServiceRepository(
        @NonNull ParameterRepository parameterRepository,
        @NonNull JPAScreeningSheetServiceRepositoryMapper mapper) {
        return new JPAScreeningSheetServiceRepository(mapper, parameterRepository);
    }

    @Bean
    ScreeningSheetServiceMapper screeningSheetServiceMapper() {
        return new ScreeningSheetServiceMapperImpl();
    }

    @Bean
    ScreeningSheetService screeningSheetService(
        @NonNull ScreeningSheetServiceMapper mapper,
        @NonNull ScreeningSheetServiceRepository repository,
        @NonNull ScreeningSheetParameterProvider screeningDataProvider,
        @NonNull SelectionVectorProvider selektionsVectorProvider) {
        return new ScreeningSheetServiceImpl(mapper, repository, screeningDataProvider, selektionsVectorProvider);
    }

    @Bean
    ScreeningSheetController screeningSheetController(
        @NonNull ResourceMapper mapper,
        @NonNull ScreeningSheetService screeningSheetService) {
        return new ScreeningSheetController(mapper, screeningSheetService);
    }

    private <T> Map<String, T> getTenantImplementierungen(ListableBeanFactory beanFactory, Class<T> clz) {
        return beanFactory.getBeansOfType(clz)
            .values()
            .stream()
            .collect(Collectors.toMap(bean -> bean.getClass().getAnnotation(Tenant.class).value(), Function.identity()));
    }
}
