/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import eu.tailoringexpert.TailoringexpertMapperConfig;
import eu.tailoringexpert.domain.BaseCatalogEntity;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.repository.BaseCatalogRepository;
import lombok.Setter;
import org.mapstruct.Mapper;

/**
 * Mapper used by {@link JPABaseRequirementsProviderRepository} to convert domain and entity objects.
 *
 * @author Michael Bädorf
 */
@Mapper(config = TailoringexpertMapperConfig.class)
public abstract class JPABaseRequirementsProviderRepositoryMapper {

    @Setter
    private BaseCatalogRepository baseCatalogRepository;

    abstract Catalog<BaseRequirement> getBaseCatalog(BaseCatalogEntity entity);

}
