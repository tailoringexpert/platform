/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2023 Michael Bädorf and others
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
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.TailoringCatalogChapterEntity;
import eu.tailoringexpert.domain.TailoringRequirement;
import org.mapstruct.Mapper;

@Mapper(config = TailoringexpertMapperConfig.class)
public interface TailoringCatalogChapterEntityMapper {
    TailoringCatalogChapterEntity toEntity(Chapter<TailoringRequirement> domain);
}
