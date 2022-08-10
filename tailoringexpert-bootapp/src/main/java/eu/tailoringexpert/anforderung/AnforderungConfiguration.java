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
package eu.tailoringexpert.anforderung;

import eu.tailoringexpert.domain.ResourceMapper;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import eu.tailoringexpert.repository.ProjektRepository;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnforderungConfiguration {

    @Bean
    JPAAnforderungServiceRepositoryMapper jpaAnforderungServiceRepositoryMapper(
        @NonNull LogoRepository logoRepository,
        @NonNull DRDRepository drdRepository) {
        JPAAnforderungServiceRepositoryMapperImpl result = new JPAAnforderungServiceRepositoryMapperImpl();
        result.setLogoRepository(logoRepository);
        result.setDrdRepository(drdRepository);
        return result;
    }

    @Bean
    AnforderungServiceRepository anforderungServiceRepository(
        @NonNull JPAAnforderungServiceRepositoryMapper mapper,
        @NonNull ProjektRepository projektRepository) {
        return new JPAAnforderungServiceRepository(mapper, projektRepository);
    }

    @Bean
    AnforderungService anforderungService(
        @NonNull AnforderungServiceRepository repository) {
        return new AnforderungServiceImpl(repository);
    }

    @Bean
    AnforderungController anforderungController(
        @NonNull ResourceMapper mapper,
        @NonNull AnforderungService anforderungService,
        @NonNull AnforderungServiceRepository anforderungServiceRepository) {
        return new AnforderungController(mapper, anforderungService, anforderungServiceRepository);
    }
}
