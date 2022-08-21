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
package eu.tailoringexpert.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.Tenants;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
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
public class CatalogConfiguration {

    @Bean
    JPACatalogServiceRepositoryMapper katalogDefinitionServiceRepositoryMapper(
        @NonNull LogoRepository logoRepository,
        @NonNull DRDRepository drdRepository) {
        JPACatalogServiceRepositoryMapperImpl result = new JPACatalogServiceRepositoryMapperImpl();
        result.setLogoRepository(logoRepository);
        result.setDrdRepository(drdRepository);
        return result;
    }


    @Bean
    CatalogServiceRepository katalogServiceRepository(
        @NonNull JPACatalogServiceRepositoryMapper mapper,
        @NonNull BaseCatalogRepository katalogDefinitionRepository,
        @NonNull DRDRepository drdRepository) {
        return new JPACatalogServiceRepository(mapper, katalogDefinitionRepository, drdRepository);
    }


    @Bean
    CatalogService katalogService(@NonNull CatalogServiceRepository catalogServiceRepository,
                                  @NonNull @Qualifier("katalogDokumentService") DocumentService documentService) {
        return new CatalogServiceImpl(catalogServiceRepository, documentService);
    }

    @Bean
    CatalogController katalogDefinitionController(
        @NonNull ResourceMapper mapper,
        @NonNull CatalogService catalogService,
        @NonNull BaseCatalogRepository katalogDefinitionRepository,
        @NonNull Function<String, MediaType> mediaTypeProvider,
        @NonNull ObjectMapper objectMapper) {
        return new CatalogController(mapper, catalogService, katalogDefinitionRepository, mediaTypeProvider, objectMapper);
    }

    @Bean
    @Primary
    DocumentService katalogDokumentService(@NonNull ListableBeanFactory beanFactory) {
        Map<String, DocumentService> services = Tenants.get(beanFactory, DocumentService.class);
        return new TenantDocumentService(services);
    }

}
