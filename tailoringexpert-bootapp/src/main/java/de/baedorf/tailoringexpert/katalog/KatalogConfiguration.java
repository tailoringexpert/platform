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
package de.baedorf.tailoringexpert.katalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.baedorf.tailoringexpert.Tenants;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.repository.DRDRepository;
import de.baedorf.tailoringexpert.repository.KatalogRepository;
import de.baedorf.tailoringexpert.repository.LogoRepository;
import lombok.NonNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.function.Function;

@Configuration
public class KatalogConfiguration {

    @Bean
    JPAKatalogServiceRepositoryMapper katalogDefinitionServiceRepositoryMapper(
        @NonNull LogoRepository logoRepository,
        @NonNull DRDRepository drdRepository) {
        JPAKatalogServiceRepositoryMapperImpl result = new JPAKatalogServiceRepositoryMapperImpl();
        result.setLogoRepository(logoRepository);
        result.setDrdRepository(drdRepository);
        return result;
    }


    @Bean
    KatalogServiceRepository katalogServiceRepository(
        @NonNull JPAKatalogServiceRepositoryMapper mapper,
        @NonNull KatalogRepository katalogDefinitionRepository,
        @NonNull DRDRepository drdRepository) {
        return new JPAKatalogServiceRepository(mapper, katalogDefinitionRepository, drdRepository);
    }


    @Bean
    KatalogService katalogService(@NonNull KatalogServiceRepository katalogServiceRepository,
                                  @NonNull @Qualifier("katalogDokumentService") DokumentService dokumentService) {
        return new KatalogServiceImpl(katalogServiceRepository, dokumentService);
    }

    @Bean
    KatalogController katalogDefinitionController(
        @NonNull ResourceMapper mapper,
        @NonNull KatalogService katalogService,
        @NonNull KatalogRepository katalogDefinitionRepository,
        @NonNull Function<String, MediaType> mediaTypeProvider,
        @NonNull ObjectMapper objectMapper) {
        return new KatalogController(mapper, katalogService, katalogDefinitionRepository, mediaTypeProvider, objectMapper);
    }

    @Bean
    @Primary
    DokumentService katalogDokumentService(@NonNull ListableBeanFactory beanFactory) {
        Map<String, DokumentService> services = Tenants.get(beanFactory, DokumentService.class);
        return new TenantDokumentService(services);
    }

}
