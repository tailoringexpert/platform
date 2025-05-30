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
package eu.tailoringexpert.tailoring;


import eu.tailoringexpert.Tenant;
import eu.tailoringexpert.Tenants;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Document;
import eu.tailoringexpert.domain.MediaTypeProvider;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
import eu.tailoringexpert.repository.ApplicableDocumentRepository;
import eu.tailoringexpert.repository.DokumentSigneeRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import eu.tailoringexpert.repository.TailoringIdentifierProviderRepository;
import eu.tailoringexpert.repository.TailoringRepository;
import eu.tailoringexpert.requirement.RequirementService;
import lombok.NonNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.Phase.D;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

@Configuration
public class TailoringConfiguration {

    @Bean
    JPATailoringServiceRepositoryMapper jpaTailoringServiceRepositoryMapper(
        @NonNull LogoRepository logoRepository,
        @NonNull ApplicableDocumentRepository applicableDocumentRepository) {
        JPATailoringServiceRepositoryMapperGenerated result = new JPATailoringServiceRepositoryMapperGenerated();
        result.setLogoRepository(logoRepository);
        result.setApplicableDocumentRepository(applicableDocumentRepository);
        return result;
    }

    @Bean
    DocumentCreator tailoringCatalogExcelDocumentCreator() {
        return new TailoringCatalogExcelDocumentCreator();
    }

    @Bean
    @Primary
    DocumentService documentService(@NonNull ListableBeanFactory beanFactory) {
        Map<String, DocumentService> services = Tenants.get(beanFactory, DocumentService.class);
        return new TenantDocumentService(services);
    }

    @Bean
    TailoringServiceRepository tailoringServiceRepository(@NonNull JPATailoringServiceRepositoryMapper mapper,
                                                          @NonNull ProjectRepository projectRepository,
                                                          @NonNull TailoringRepository tailoringRepository,
                                                          @NonNull SelectionVectorProfileRepository selectionVectorProfileRepository,
                                                          @NonNull DokumentSigneeRepository dokumentSigneeRepository) {
        return new JPATailoringServiceRepository(mapper, projectRepository, tailoringRepository, selectionVectorProfileRepository, dokumentSigneeRepository);
    }

    @Bean
    TailoringDeletablePredicateRepository tailoringDeletablePredicateRepository(
        @NonNull ProjectRepository projectRepository) {
        return new JPATailoringDeletablePredicateRepository(projectRepository);
    }

    @Bean
    TailoringServiceMapper tailoringServiceMapper() {
        return new TailoringServiceMapperGenerated();
    }

    @Bean
    DefaultTailoringDeletablePredicate defaultTailoringDeletablePredicate(
        @NonNull TailoringDeletablePredicateRepository repository) {
        return new DefaultTailoringDeletablePredicate(repository);
    }

    @Bean
    TailoringService tailoringService(
        @NonNull TailoringServiceRepository repository,
        @NonNull TailoringServiceMapper mapper,
        @NonNull TailoringDeletablePredicate tailoringDeletablePredicate,
        @NonNull DocumentService documentService,
        @NonNull RequirementService requirementService,
        @NonNull Function<byte[], Map<String, Collection<ImportRequirement>>> tailoringAnforderungFileReader,
        @NonNull AttachmentService attachmentService
    ) {
        return new TailoringServiceImpl(
            repository,
            mapper,
            tailoringDeletablePredicate,
            documentService,
            requirementService,
            tailoringAnforderungFileReader,
            attachmentService
        );
    }

    @Bean
    BiPredicate<String, Collection<Phase>> drdAnwendbarPraedikat() {
        Map<Phase, Collection<String>> phase2Meilensteine = Map.ofEntries(
            new SimpleEntry<>(ZERO, unmodifiableCollection(asList("MDR"))),
            new SimpleEntry<>(A, unmodifiableCollection(asList("PRR", "SRR"))),
            new SimpleEntry<>(B, unmodifiableCollection(asList("PDR"))),
            new SimpleEntry<>(C, unmodifiableCollection(asList("CDR"))),
            new SimpleEntry<>(D, unmodifiableCollection(asList("MRR", "TRR", "QR", "CCB", "MPCB", "AR", "DRB", "DAR", "FRR", "LRR"))),
            new SimpleEntry<>(E, unmodifiableCollection(asList("AR", "ORR", "GS upgrades", "SW upgrades", "CRR", "ELR"))),
            new SimpleEntry<>(F, unmodifiableCollection(asList("EOM", "MCR")))
        );

        return new DRDApplicablePredicate(phase2Meilensteine);
    }

    @Bean
    Function<String, MediaType> mediaTypeProvider() {
        return new MediaTypeProvider();
    }

    @Bean
    TailoringController tailoringController(
        @NonNull ResourceMapper mapper,
        @NonNull TailoringService tailoringService,
        @NonNull TailoringServiceRepository tailoringServiceRepository,
        @NonNull AttachmentService attachmentService,
        @NonNull Function<String, MediaType> mediaTypeProvider) {
        return new TailoringController(
            mapper, tailoringService, tailoringServiceRepository, attachmentService, mediaTypeProvider
        );
    }

    @Bean
    String tenantConfigHome(@Value("${tenantConfigHome}") String tenantConfigHome) {
        return tenantConfigHome;
    }

    @Bean
    Function<byte[], Map<String, Collection<ImportRequirement>>> tailoringAnforderungExcelFileReader() {
        return new TailoringRequirementExcelFileReader();
    }

    @Bean
    @Primary
    TailoringDeletablePredicate tailoringDeletablePredicate(
        @NonNull DefaultTailoringDeletablePredicate defaultTailoringDeletablePredicate,
        @NonNull ListableBeanFactory beanFactory) {
        Map<String, TailoringDeletablePredicate> predicates = getTenantImplementations(beanFactory, TailoringDeletablePredicate.class);
        return new TenantTailoringDeletablePredicate(predicates, defaultTailoringDeletablePredicate);
    }

    @Bean
    DocumentCreator tailoringCatalogPDFDocumentCreator(
        @NonNull HTMLTemplateEngine templateEngine,
        @NonNull PDFEngine pdfEngine,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider,
        @NonNull Function<Catalog<TailoringRequirement>, Collection<Document>> tailoringCatalogApplicableDocumentProvider) {
        return new TailoringCatalogPDFDocumentCreator(
            drdProvider,
            tailoringCatalogApplicableDocumentProvider,
            templateEngine,
            pdfEngine
        );
    }

    @Bean
    DocumentCreator comparisionPDFDocumentCreator(
        @NonNull HTMLTemplateEngine templateEngine,
        @NonNull PDFEngine pdfEngine,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new ComparisonPDFDocumentCreator(templateEngine, pdfEngine);
    }

    @Bean
    DocumentCreator drdPDFDocumentCreator(
        @NonNull HTMLTemplateEngine templateEngine,
        @NonNull PDFEngine pdfEngine,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new DRDPDFDocumentCreator(drdProvider, templateEngine, pdfEngine);
    }

    @Bean
    DocumentCreator tailoringCatalogSpreadsheetCreator() {
        return new TailoringCatalogExcelDocumentCreator();
    }

    @Bean
    DocumentCreator cmSpreadsheetDocumentCreator(
        @NonNull RendererRequestConfigurationSupplier requestConfigurationSupplier,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new CMExcelDocumentCreator(requestConfigurationSupplier, drdProvider);
    }

    @Bean
    DocumentCreator cmRequirementsSpreadsheetDocumentCreator(
        @NonNull RendererRequestConfigurationSupplier requestConfigurationSupplier,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new CMRequirementsExcelDocumentCreator(requestConfigurationSupplier, drdProvider);
    }

    @Bean
    DocumentCreator cmPDFDocumentCreator(
        @NonNull HTMLTemplateEngine templateEngine,
        @NonNull PDFEngine pdfEngine,
        @NonNull BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new CMPDFDocumentCreator(drdProvider, templateEngine, pdfEngine);
    }


    @Bean
    BiFunction<String, String, Path> tailoringPathProvider(
        @NonNull @Value("${attachmentHome}") String basedir,
        @NonNull TailoringIdentifierProviderRepository tailoringIdentifierProvider
    ) {
        return new TenantTailoringPathProvider(basedir, tailoringIdentifierProvider);
    }

    @Bean
    AttachmentService attachmentService(
        @NonNull @Qualifier("tailoringPathProvider") BiFunction<String, String, Path> tailoringPathProvider
    ) throws Exception {
        return new TenantAttachmentService(tailoringPathProvider, MessageDigest.getInstance("SHA-256"));
    }

    private <T> Map<String, T> getTenantImplementations(ListableBeanFactory beanFactory, Class<T> clz) {
        return beanFactory.getBeansOfType(clz)
            .values()
            .stream()
            // Defaultimplementation has no tenant annotation!
            .filter(bean -> Objects.nonNull(bean.getClass().getAnnotation(Tenant.class)))
            .collect(Collectors.toMap(bean -> bean.getClass().getAnnotation(Tenant.class).value(), Function.identity()));
    }


}
