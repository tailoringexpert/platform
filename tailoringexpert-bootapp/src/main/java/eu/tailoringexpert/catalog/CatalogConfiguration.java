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
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
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
    JPACatalogServiceRepositoryMapper catalogServiceRepositoryMapper(
        @NonNull LogoRepository logoRepository,
        @NonNull DRDRepository drdRepository) {
        JPACatalogServiceRepositoryMapperGenerated result = new JPACatalogServiceRepositoryMapperGenerated();
        result.setLogoRepository(logoRepository);
        result.setDrdRepository(drdRepository);
        return result;
    }


    @Bean
    CatalogServiceRepository catalogServiceRepository(
        @NonNull JPACatalogServiceRepositoryMapper mapper,
        @NonNull BaseCatalogRepository baseCatalogRepository,
        @NonNull DRDRepository drdRepository) {
        return new JPACatalogServiceRepository(mapper, baseCatalogRepository, drdRepository);
    }


    @Bean
    CatalogService catalogService(@NonNull CatalogServiceRepository catalogServiceRepository,
                                  @NonNull @Qualifier("catalogDocumentService") DocumentService catalogDocumentService) {
        return new CatalogServiceImpl(catalogServiceRepository, catalogDocumentService);
    }

    @Bean
    CatalogController catalogController(
        @NonNull ResourceMapper mapper,
        @NonNull CatalogService catalogService,
        @NonNull Function<String, MediaType> mediaTypeProvider,
        @NonNull ObjectMapper objectMapper) {
        return new CatalogController(mapper, catalogService, mediaTypeProvider, objectMapper);
    }

    @Bean
    JPADocumentServiceRepositoryMapper documentServiceRepositoryMapper() {
        return new JPADocumentServiceRepositoryMapperGenerated();
    }

    @Bean
    DocumentServiceRepository documentServiceRepository(
        @NonNull JPADocumentServiceRepositoryMapper mapper,
        @NonNull SelectionVectorProfileRepository selectionVectorProfileRepository) {
        return new JPADocumentServiceRepository(mapper, selectionVectorProfileRepository);
    }

    @Bean
    @Primary
    DocumentService catalogDocumentService(@NonNull ListableBeanFactory beanFactory) {
        Map<String, DocumentService> services = Tenants.get(beanFactory, DocumentService.class);
        return new TenantDocumentService(services);
    }

    @Bean
    BaseCatalogPDFDocumentCreator baseCatalogPDFDocumentCreator(
        @NonNull HTMLTemplateEngine templateEngine,
        @NonNull PDFEngine pdfEngine) {
        return new BaseCatalogPDFDocumentCreator(templateEngine, pdfEngine);
    }

    @Bean
    DRDProvider baseDRDdProvider() {
        return new DRDProvider();
    }

    @Bean
    BaseDRDPDFDocumentCreator baseDRDPDFDocumentCreator(@NonNull HTMLTemplateEngine templateEngine,
                                                        @NonNull PDFEngine pdfEngine,
                                                        @NonNull DRDProvider drdProvider) {
        return new BaseDRDPDFDocumentCreator(templateEngine, pdfEngine, drdProvider);
    }


}
