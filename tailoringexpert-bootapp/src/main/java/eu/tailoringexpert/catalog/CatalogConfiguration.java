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
import com.github.difflib.text.DiffRowGenerator;
import eu.tailoringexpert.Tenants;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDProvider;
import eu.tailoringexpert.domain.Document;
import eu.tailoringexpert.domain.Identifier;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Reference;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.ApplicableDocumentRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import eu.tailoringexpert.repository.TailoringCatalogRepository;
import lombok.NonNull;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.*;

import static java.util.List.of;
import static java.util.function.Function.identity;

@Configuration
public class CatalogConfiguration {

    @Bean
    JPACatalogServiceRepositoryMapper catalogServiceRepositoryMapper(
        @NonNull LogoRepository logoRepository,
        @NonNull ApplicableDocumentRepository applicableDocumentRepository,
        @NonNull DRDRepository drdRepository) {
        JPACatalogServiceRepositoryMapperGenerated result = new JPACatalogServiceRepositoryMapperGenerated();
        result.setLogoRepository(logoRepository);
        result.setApplicableDocumentRepository(applicableDocumentRepository);
        result.setDrdRepository(drdRepository);
        return result;
    }


    @Bean
    CatalogServiceRepository catalogServiceRepository(
        @NonNull JPACatalogServiceRepositoryMapper mapper,
        @NonNull BaseCatalogRepository baseCatalogRepository,
        @NonNull ApplicableDocumentRepository applicableDocumentRepository,
        @NonNull DRDRepository drdRepository,
        @NonNull TailoringCatalogRepository tailoringCatalogRepository) {
        return new JPACatalogServiceRepository(
            mapper,
            baseCatalogRepository,
            applicableDocumentRepository,
            drdRepository,
            tailoringCatalogRepository
        );
    }


    @Bean
    CatalogService catalogService(
        @NonNull CatalogServiceRepository catalogServiceRepository,
        @NonNull @Qualifier("catalogDocumentService") DocumentService catalogDocumentService,
        @NonNull @Qualifier("excel2CatalogConverter") Function<byte[], Catalog<BaseRequirement>> file2CatalogConverter

    ) {
        return new CatalogServiceImpl(catalogServiceRepository, catalogDocumentService, file2CatalogConverter);
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
        @NonNull Function<Catalog<BaseRequirement>, Collection<Document>> baseCatalogApplicableDocumentProvider,
        @NonNull Predicate<BaseRequirement> baseRequirementSelectedPredicate,
        @NonNull BiPredicate<String, Collection<Phase>> drdAnwendbarPraedikat,
        @NonNull HTMLTemplateEngine templateEngine,
        @NonNull PDFEngine pdfEngine) {
        return new BaseCatalogPDFDocumentCreator(
            baseCatalogApplicableDocumentProvider,
            new DRDProvider<>(baseRequirementSelectedPredicate, drdAnwendbarPraedikat),
            templateEngine,
            pdfEngine);
    }

    @Bean
    BaseDRDPDFDocumentCreator baseDRDPDFDocumentCreator(@NonNull HTMLTemplateEngine templateEngine,
                                                        @NonNull PDFEngine pdfEngine,
                                                        @NonNull BiFunction<Chapter<BaseRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider) {
        return new BaseDRDPDFDocumentCreator(drdProvider, templateEngine, pdfEngine);
    }

    @Bean
    BiConsumer<Catalog<BaseRequirement>, Sheet> drdSheetCreator() {
        return new DRDSheetCreator();
    }

    @Bean
    BiConsumer<Catalog<BaseRequirement>, Sheet> documentSheetCreator(
        @NonNull Function<Catalog<BaseRequirement>, Collection<Document>> baseCatalogApplicableDocumentProvider) {
        return new DocumentSheetCreator(baseCatalogApplicableDocumentProvider);
    }

    @Bean
    BiConsumer<Catalog<BaseRequirement>, Sheet> logoSheetCreator() {
        return new LogoSheetCreator();
    }

    @Bean
    BiConsumer<Catalog<BaseRequirement>, Sheet> requirementSheetCreator() {
        return new RequirementSheetCreator();
    }

    @Bean
    BaseCatalogExcelDocumentCreator baseCatalogExportExcelDocumentCreator(
        @NonNull @Qualifier("requirementSheetCreator") BiConsumer<Catalog<BaseRequirement>, Sheet> requirementSheetCreator,
        @NonNull @Qualifier("drdSheetCreator") BiConsumer<Catalog<BaseRequirement>, Sheet> drdSheetCreator,
        @NonNull @Qualifier("documentSheetCreator") BiConsumer<Catalog<BaseRequirement>, Sheet> documentSheetCreator,
        @NonNull @Qualifier("logoSheetCreator") BiConsumer<Catalog<BaseRequirement>, Sheet> logoSheetCreator
    ) {
        return new BaseCatalogExcelDocumentCreator(
            requirementSheetCreator,
            drdSheetCreator,
            documentSheetCreator,
            logoSheetCreator
        );
    }


    @Bean
    Function<Sheet, Map<String, DRD>> toDRDMappingFunction() {
        return new ToDRDMappingFunction();
    }

    @Bean
    Function<Sheet, Map<String, Logo>> toLogoMappingFunction() {
        return new ToLogoMappingFunction();
    }

    @Bean
    Function<String, Identifier> toIdentifierFunction() {
        return new ToIdentifierFunction();
    }

    @Bean
    BiFunction<String, Map<String, Logo>, Logo> toLogoFunction() {
        return new ToLogoFunction();
    }

    @Bean
    Function<Sheet, Map<String, Document>> toDocumentFunction() {
        return new ToDocumentMappingFunction();
    }

    @Bean
    BiFunction<String, Logo, Reference> toReferenceFunction() {
        return new ToReferenceFunction();
    }

    @Bean
    BiConsumer<Chapter<BaseRequirement>, Map<String, Chapter<BaseRequirement>>> buildingChapterConsumer() {
        return new BuildingChapterConsumer();
    }

    @Bean
    Function<Sheet, Chapter<BaseRequirement>> toChapterFunction(
        @NonNull @Qualifier("toDRDMappingFunction") Function<Sheet, Map<String, DRD>> toDRDMappingFunction,
        @NonNull @Qualifier("toLogoMappingFunction") Function<Sheet, Map<String, Logo>> toLogoMappingFunction,
        @NonNull @Qualifier("toDocumentFunction") Function<Sheet, Map<String, Document>> toDocumentFunction,
        @NonNull @Qualifier("toIdentifierFunction") Function<String, Identifier> toIdentifierFunction,
        @NonNull @Qualifier("toLogoFunction") BiFunction<String, Map<String, Logo>, Logo> toLogoFunction,
        @NonNull @Qualifier("toReferenceFunction") BiFunction<String, Logo, Reference> toReferenceFunction,
        @NonNull @Qualifier("buildingChapterConsumer") BiConsumer<Chapter<BaseRequirement>, Map<String, Chapter<BaseRequirement>>> buildingChapterConsumer
    ) {
        return new ToChapterFunction(
            toDRDMappingFunction,
            toLogoMappingFunction,
            toDocumentFunction,
            toIdentifierFunction,
            toLogoFunction,
            toReferenceFunction,
            buildingChapterConsumer
        );
    }

    @Bean
    Function<byte[], Catalog<BaseRequirement>> excel2CatalogConverter(
        @NonNull @Qualifier("toChapterFunction") Function<Sheet, Chapter<BaseRequirement>> toChapterFunction
    ) {
        return new Excel2CatalogConverter(toChapterFunction);
    }

    @Bean
    TextDiff textDiff(DiffRowGenerator diffRowGenerator) {
        return new DifflibTextDiff(diffRowGenerator);
    }

    @Bean
    DiffRowGenerator diffRowGenerator() {
        return DiffRowGenerator.create()
            .reportLinesUnchanged(false)
            .showInlineDiffs(true)
            .mergeOriginalRevised(true)
            .inlineDiffByWord(true)
            .ignoreWhiteSpaces(true)
            .lineNormalizer(identity())
            .oldTag((tag, f) -> f ? "<span class='requirement-old'>" : "</span>")
            .newTag((tag, f) -> f ? "<span class='requirement-new'>" : "</span>")
            .build();
    }

    @Bean
    ToRevisedBaseCatalogFunction toRevisedBaseCatalogFunction(@NonNull TextDiff diffRowGenerator) {
        return new ToRevisedBaseCatalogFunction(diffRowGenerator);

    }
}
