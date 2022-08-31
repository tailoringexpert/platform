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

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.project.JPAProjectServiceRepository;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.ThymeleafTemplateEngine;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.DokumentSigneeRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.screeningsheet.PlattformScreeningSheetParameterProvider;
import eu.tailoringexpert.screeningsheet.PlattformSelectionVectorProvider;
import eu.tailoringexpert.screeningsheet.SelectionVectorProvider;
import eu.tailoringexpert.tailoring.CMPDFDocumentCreator;
import eu.tailoringexpert.tailoring.CMExcelDocumentCreator;
import eu.tailoringexpert.tailoring.ComparisonPDFDocumentCreator;
import eu.tailoringexpert.tailoring.DRDPDFDocumentCreator;
import eu.tailoringexpert.tailoring.DocumentCreator;
import eu.tailoringexpert.tailoring.DocumentService;
import eu.tailoringexpert.tailoring.PlattformDocumentService;
import eu.tailoringexpert.tailoring.TailoringCatalogPDFDocumentCreator;
import eu.tailoringexpert.tailoring.TailoringCatalogExcelDocumentCreator;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.UrlTemplateResolver;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

@Log4j2
@Configuration
public class PlattformConfiguration {

    private static final String TEMPLATE = "template";

    @Bean
    CacheManager plattformCacheManager() {
        log.info("Creating cache manager plattform");
        return new @Tenant("plattform") ConcurrentMapCacheManager(
            BaseCatalogRepository.CACHE_BASECATALOG,
            BaseCatalogRepository.CACHE_BASECATALOGLIST,
            JPAProjectServiceRepository.CACHE_BASECATALOG,
            LogoRepository.CACHE_LOGO,
            DokumentSigneeRepository.CACHE_DOCUMENTSIGNEE
        ) {
        };
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
    Map<String, String> plattform(@Value("#{${tenant.plattform}}") Map<String, String> plattform) {
        return plattform;
    }

    @Bean
    PDFEngine plattformPdfEngine(@Qualifier("plattform") Map<String, String> plattform) {
        return new PDFEngine("TailoringExpert", plattform.get(TEMPLATE));
    }

    @SneakyThrows
    @Bean
    ThymeleafTemplateEngine plattformTemplateEngine(@NonNull @Qualifier("plattform") Map<String, String> plattform) {
        SpringTemplateEngine springTemplateEngine = new @Tenant("plattform") SpringTemplateEngine() {
        };
        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setPrefix(plattform.get(TEMPLATE));
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setTemplateMode(HTML);
        fileTemplateResolver.setCharacterEncoding(UTF_8.toString());
        fileTemplateResolver.setOrder(1);
        fileTemplateResolver.setCheckExistence(true);
        springTemplateEngine.addTemplateResolver(fileTemplateResolver);

        UrlTemplateResolver drdResolver = new UrlTemplateResolver();
        drdResolver.setPrefix(new URL(plattform.get("drd")).toString());
        drdResolver.setCacheable(false);
        drdResolver.setSuffix(".html");
        drdResolver.setTemplateMode(HTML);
        drdResolver.setCharacterEncoding(UTF_8.toString());
        drdResolver.setOrder(2);
        drdResolver.setCheckExistence(true);
        springTemplateEngine.addTemplateResolver(drdResolver);

        return new @Tenant("plattform") ThymeleafTemplateEngine(springTemplateEngine) {
        };
    }

    @Bean
    Function<String, File> plattformExcelSupplier(@NonNull @Qualifier("plattform") Map<String, String> plattform) {
        return new Function<String, File>() {
            @SneakyThrows
            @Override
            public File apply(String s) {
                return Paths.get(plattform.get(TEMPLATE) + s).toFile();
            }
        };
    }


    @Bean
    DocumentCreator plattformTailoringCatalogDocumentCreator(
        @NonNull @Qualifier("plattformTemplateEngine") ThymeleafTemplateEngine templateEngine,
        @NonNull @Qualifier("plattformPdfEngine") PDFEngine pdfEngine,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new TailoringCatalogPDFDocumentCreator(templateEngine, pdfEngine, drdProvider);
    }

    @Bean
    DocumentCreator plattformCMDocumentCreator(
        @NonNull @Qualifier("plattformTemplateEngine") ThymeleafTemplateEngine templateEngine,
        @NonNull @Qualifier("plattformPdfEngine") PDFEngine pdfEngine,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new CMPDFDocumentCreator(templateEngine, pdfEngine, drdProvider);
    }

    @Bean
    DocumentCreator plattformDRDDocumentCreator(
        @NonNull @Qualifier("plattformTemplateEngine") ThymeleafTemplateEngine templateEngine,
        @NonNull @Qualifier("plattformPdfEngine") PDFEngine pdfEngine,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new DRDPDFDocumentCreator(templateEngine, pdfEngine, drdProvider);
    }

    @Bean
    DocumentCreator plattformComparisionDocumentCreator(
        @NonNull @Qualifier("plattformTemplateEngine") ThymeleafTemplateEngine templateEngine,
        @NonNull @Qualifier("plattformPdfEngine") PDFEngine pdfEngine,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new ComparisonPDFDocumentCreator(templateEngine, pdfEngine);
    }

    @Bean
    DocumentCreator plattformCMSpreadsheetCreator(
        @NonNull @Qualifier("plattformExcelSupplier") Function<String, File> plattformExcelSupplier,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new CMExcelDocumentCreator(plattformExcelSupplier, drdProvider);
    }

    @Bean
    DocumentCreator plattformTailoringCatalogSpreadsheetCreator() {
        return new TailoringCatalogExcelDocumentCreator();
    }

    @Bean
    DocumentService plattformDocumentService(
        @NonNull @Qualifier("plattformTailoringCatalogDocumentCreator") DocumentCreator tailoringCatalogDocumentCreator,
        @NonNull @Qualifier("plattformComparisionDocumentCreator") DocumentCreator comparisionDocumentCreator,
        @NonNull @Qualifier("plattformDRDDocumentCreator") DocumentCreator drdDocumentCreator,
        @NonNull @Qualifier("plattformCMDocumentCreator") DocumentCreator cmDocumentCreator,
        @NonNull @Qualifier("plattformCMSpreadsheetCreator") DocumentCreator cmSpreadsheetCreator,
        @NonNull @Qualifier("plattformTailoringCatalogSpreadsheetCreator") DocumentCreator tailoringCatalogSpreadsheetCreator) {
        return new PlattformDocumentService(
            tailoringCatalogDocumentCreator,
            cmDocumentCreator,
            comparisionDocumentCreator,
            drdDocumentCreator,
            tailoringCatalogSpreadsheetCreator,
            cmSpreadsheetCreator);
    }

}
