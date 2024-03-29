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
package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.TailoringexpertMapperConfig;
import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting data object in @see {@link ScreeningSheetService}.
 *
 * @author Michael Bädorf
 */
@Mapper(config = TailoringexpertMapperConfig.class)
public interface ScreeningSheetServiceMapper {

    @Mapping(target = "value", source = "label")
    ScreeningSheetParameter createScreeningSheet(Parameter parameter);
}
