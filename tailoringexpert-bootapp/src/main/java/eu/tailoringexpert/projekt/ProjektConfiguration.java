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
package eu.tailoringexpert.projekt;

import eu.tailoringexpert.repository.KatalogRepository;
import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.ProjektRepository;
import eu.tailoringexpert.screeningsheet.ScreeningSheetService;
import eu.tailoringexpert.tailoring.TailoringService;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjektConfiguration {

    @Bean
    JPAProjektServiceRepositoryMapper jpaProjektServiceRepositoryMapper(
        @NonNull KatalogRepository katalogRepository,
        @NonNull LogoRepository logoRepository,
        @NonNull DRDRepository drdRepository) {
        JPAProjektServiceRepositoryMapperImpl result = new JPAProjektServiceRepositoryMapperImpl();
        result.setKatalogRepository(katalogRepository);
        result.setLogoRepository(logoRepository);
        result.setDrdRepository(drdRepository);
        return result;
    }

    @Bean
    ProjektServiceRepository projektServiceRepository(
        @NonNull JPAProjektServiceRepositoryMapper mapper,
        @NonNull ProjektRepository projektRepository,
        @NonNull KatalogRepository katalogDefintionRepository) {
        return new JPAProjektServiceRepository(mapper, projektRepository, katalogDefintionRepository);
    }

    @Bean
    ProjektService projektService(@NonNull ProjektServiceRepository repository,
                                  @NonNull ScreeningSheetService screeningSheetService,
                                  @NonNull TailoringService tailoringService) {
        return new ProjektServiceImpl(repository, screeningSheetService, tailoringService);
    }

    @Bean
    ProjektController projektController(
        @NonNull ResourceMapper mapper,
        @NonNull ProjektService projektService,
        @NonNull ProjektServiceRepository projektServiceRepository,
        @NonNull TailoringService tailoringService) {
        return new ProjektController(mapper, projektService, projektServiceRepository, tailoringService);
    }

}
