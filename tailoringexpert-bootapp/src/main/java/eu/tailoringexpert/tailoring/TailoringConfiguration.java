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


import eu.tailoringexpert.Tenants;
import eu.tailoringexpert.domain.MediaTypeProvider;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.requirement.RequirementService;
import eu.tailoringexpert.repository.DokumentSigneeRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.ProjectRepository;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import eu.tailoringexpert.repository.TailoringRepository;
import lombok.NonNull;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

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
        @NonNull LogoRepository logoRepository) {
        JPATailoringServiceRepositoryMapperImpl result = new JPATailoringServiceRepositoryMapperImpl();
        result.setLogoRepository(logoRepository);
        return result;
    }

    @Bean
    DocumentCreator projektPhaseKatalogSpreadsheetCreator() {
        return new TailoringCatalogSpreadsheetCreator();
    }

    @Bean
    @Primary
    DocumentService dokumentService(@NonNull ListableBeanFactory beanFactory) {
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
    TailoringServiceMapper tailoringServiceMapper() {
        return new TailoringServiceMapperImpl();
    }

    @Bean
    TailoringService tailoringService(
        @NonNull TailoringServiceRepository repository,
        @NonNull TailoringServiceMapper mapper,
        @NonNull DocumentService documentService,
        @NonNull RequirementService requirementService,
        @NonNull Function<byte[], Map<String, Collection<ImportRequirement>>> tailoringAnforderungFileReader) {
        return new TailoringServiceImpl(repository, mapper, documentService, requirementService, tailoringAnforderungFileReader);
    }

    Map<String, String> arzs(@Value("#{${tenant.arzs}}") Map<String, String> arzs) {
        return arzs;
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
    BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider(
        @NonNull BiPredicate<String, Collection<Phase>> drdAnwendbarPraedikat) {
        return new DRDProvider(drdAnwendbarPraedikat);
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
        @NonNull Function<String, MediaType> mediaTypeProvider) {
        return new TailoringController(mapper, tailoringService, tailoringServiceRepository, mediaTypeProvider);
    }

    @Bean
    String tenantConfigDir(@Value("${tenantConfigDir}") String tenantConfigDir) {
        return tenantConfigDir;
    }

    @Bean
    Function<byte[], Map<String, Collection<ImportRequirement>>> tailoringAnforderungExcelFileReader() {
        return new TailoringRequirementExcelFileReader();
    }

}
