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
package de.baedorf.tailoringexpert.tailoring;


import de.baedorf.tailoringexpert.Tenants;
import de.baedorf.tailoringexpert.domain.MediaTypeProvider;
import de.baedorf.tailoringexpert.domain.ResourceMapper;
import de.baedorf.tailoringexpert.domain.DRD;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Phase;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.anforderung.AnforderungService;
import de.baedorf.tailoringexpert.repository.DokumentZeichnerRepository;
import de.baedorf.tailoringexpert.repository.LogoRepository;
import de.baedorf.tailoringexpert.repository.ProjektRepository;
import de.baedorf.tailoringexpert.repository.SelektionsVektorProfilRepository;
import de.baedorf.tailoringexpert.repository.TailoringRepository;
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

import static de.baedorf.tailoringexpert.domain.Phase.A;
import static de.baedorf.tailoringexpert.domain.Phase.B;
import static de.baedorf.tailoringexpert.domain.Phase.C;
import static de.baedorf.tailoringexpert.domain.Phase.D;
import static de.baedorf.tailoringexpert.domain.Phase.E;
import static de.baedorf.tailoringexpert.domain.Phase.F;
import static de.baedorf.tailoringexpert.domain.Phase.ZERO;
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
    DokumentCreator projektPhaseKatalogSpreadsheetCreator() {
        return new TailoringKatalogSpreadsheetCreator();
    }

    @Bean
    @Primary
    DokumentService dokumentService(@NonNull ListableBeanFactory beanFactory) {
        Map<String, DokumentService> services = Tenants.get(beanFactory, DokumentService.class);
        return new TenantDokumentService(services);
    }

    @Bean
    TailoringServiceRepository tailoringServiceRepository(@NonNull JPATailoringServiceRepositoryMapper mapper,
                                                          @NonNull ProjektRepository projektRepository,
                                                          @NonNull TailoringRepository tailoringRepository,
                                                          @NonNull SelektionsVektorProfilRepository selektionsVektorProfilRepository,
                                                          @NonNull DokumentZeichnerRepository dokumentZeichnerRepository) {
        return new JPATailoringServiceRepository(mapper, projektRepository, tailoringRepository, selektionsVektorProfilRepository, dokumentZeichnerRepository);
    }

    @Bean
    TailoringServiceMapper tailoringServiceMapper() {
        return new TailoringServiceMapperImpl();
    }

    @Bean
    TailoringService tailoringService(
        @NonNull TailoringServiceRepository repository,
        @NonNull TailoringServiceMapper mapper,
        @NonNull DokumentService dokumentService,
        @NonNull AnforderungService anforderungService,
        @NonNull Function<byte[], Map<String, Collection<ImportAnforderung>>> tailoringAnforderungFileReader) {
        return new TailoringServiceImpl(repository, mapper, dokumentService, anforderungService, tailoringAnforderungFileReader);
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

        return new DRDAnwendbarPraedikat(phase2Meilensteine);
    }

    @Bean
    BiFunction<Kapitel<TailoringAnforderung>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider(
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
    Function<byte[], Map<String, Collection<ImportAnforderung>>> tailoringAnforderungExcelFileReader() {
        return new TailoringAnforderungExcelFileReader();
    }

}
