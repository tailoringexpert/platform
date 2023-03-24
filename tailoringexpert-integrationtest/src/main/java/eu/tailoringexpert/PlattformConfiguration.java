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
package eu.tailoringexpert;

import eu.tailoringexpert.catalog.BaseCatalogPDFDocumentCreator;
import eu.tailoringexpert.catalog.BaseDRDPDFDocumentCreator;
import eu.tailoringexpert.project.JPAProjectServiceRepository;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.DokumentSigneeRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.screeningsheet.PlattformScreeningSheetParameterProvider;
import eu.tailoringexpert.screeningsheet.PlattformSelectionVectorProvider;
import eu.tailoringexpert.screeningsheet.SelectionVectorProvider;
import eu.tailoringexpert.tailoring.DocumentCreator;
import eu.tailoringexpert.tailoring.DocumentService;
import eu.tailoringexpert.tailoring.PlattformDocumentService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class PlattformConfiguration {

    @Bean
    CacheManager plattformCacheManager() {
        log.info("Creating cache manager plattform");
        return new PlattformCacheManager(
            BaseCatalogRepository.CACHE_BASECATALOG,
            BaseCatalogRepository.CACHE_BASECATALOGLIST,
            JPAProjectServiceRepository.CACHE_BASECATALOG,
            LogoRepository.CACHE_LOGO,
            DokumentSigneeRepository.CACHE_DOCUMENTSIGNEE
        );
    }

    @Bean
    SelectionVectorProvider plattformSelectionVectorProvider() {
        return new PlattformSelectionVectorProvider();
    }

    @Bean
    PlattformScreeningSheetParameterProvider plattformScreeningSheetParameterProvider() {
        return new PlattformScreeningSheetParameterProvider();
    }

    @Bean
    DocumentService plattformDocumentService(
        @NonNull @Qualifier("tailoringCatalogPDFDocumentCreator") DocumentCreator tailoringCatalogPDFDocumentCreator,
        @NonNull @Qualifier("tailoringCatalogSpreadsheetCreator") DocumentCreator tailoringCatalogSpreadsheetCreator,
        @NonNull @Qualifier("comparisionPDFDocumentCreator") DocumentCreator comparisionPDFDocumentCreator,
        @NonNull @Qualifier("drdPDFDocumentCreator") DocumentCreator drdPDFDocumentCreator,
        @NonNull @Qualifier("cmPDFDocumentCreator") DocumentCreator cmPDFDocumentCreator,
        @NonNull @Qualifier("cmSpreadsheetDocumentCreator") DocumentCreator cmSpreadsheetDocumentCreator,
        @NonNull @Qualifier("cmRequirementsSpreadsheetDocumentCreator") DocumentCreator cmRequirementsSpreadsheetDocumentCreator) {

        return new PlattformDocumentService(
            tailoringCatalogPDFDocumentCreator,
            tailoringCatalogSpreadsheetCreator,
            comparisionPDFDocumentCreator,
            drdPDFDocumentCreator,
            cmPDFDocumentCreator,
            cmSpreadsheetDocumentCreator,
            cmRequirementsSpreadsheetDocumentCreator
        );
    }

    @Bean
    eu.tailoringexpert.catalog.PlattformDocumentService plattformCatalogDocumentService(
        @NonNull BaseCatalogPDFDocumentCreator baseCatalogPDFDocumentCreator,
        @NonNull BaseDRDPDFDocumentCreator baseDRDPDFDocumentCreator) {
        return new eu.tailoringexpert.catalog.PlattformDocumentService(baseCatalogPDFDocumentCreator, baseDRDPDFDocumentCreator);
    }


}
