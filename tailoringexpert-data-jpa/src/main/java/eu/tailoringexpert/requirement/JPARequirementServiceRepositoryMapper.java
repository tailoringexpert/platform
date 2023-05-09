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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.TailoringexpertMapperConfig;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRDEntity;
import eu.tailoringexpert.domain.LogoEntity;
import eu.tailoringexpert.domain.TailoringRequirementEntity;
import eu.tailoringexpert.domain.TailoringCatalogChapterEntity;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.repository.DRDRepository;
import eu.tailoringexpert.repository.LogoRepository;
import lombok.Setter;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import static java.util.Objects.nonNull;

/**
 * Mapper used by {@link JPARequirementServiceRepository} to convert domain and entity objects.
 *
 * @author Michael Bädorf
 */
@Mapper(config = TailoringexpertMapperConfig.class)
public abstract class JPARequirementServiceRepositoryMapper {

    @Setter
    private LogoRepository logoRepository;

    @Setter
    private DRDRepository drdRepository;

    abstract TailoringRequirement toDomain(TailoringRequirementEntity entity);

    abstract void updateRequirement(TailoringRequirement domain, @MappingTarget TailoringRequirementEntity entity);

    abstract Chapter<TailoringRequirement> toDomain(TailoringCatalogChapterEntity entity);

    abstract void updateChapter(Chapter<TailoringRequirement> domain, @MappingTarget TailoringCatalogChapterEntity entity);

    LogoEntity resolve(Logo domain) {
        return nonNull(domain) ? logoRepository.findByName(domain.getName()) : null;
    }

    DRDEntity resolve(DRD domain) {
        return nonNull(domain) ? drdRepository.findByNumber(domain.getNumber()) : null;
    }
}
