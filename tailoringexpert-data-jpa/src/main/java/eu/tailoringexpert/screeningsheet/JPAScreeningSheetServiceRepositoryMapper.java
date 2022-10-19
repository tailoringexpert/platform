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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.Parameter.ParameterBuilder;
import eu.tailoringexpert.domain.ParameterEntity;
import lombok.Setter;
import lombok.SneakyThrows;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import static eu.tailoringexpert.domain.DatenType.MATRIX;
import static eu.tailoringexpert.domain.DatenType.SKALAR;
import static eu.tailoringexpert.domain.DatenType.STRING;
import static java.lang.Integer.valueOf;

/**
 * Mapper used by {@link JPAScreeningSheetServiceRepository} to convert domain and entity objects.
 *
 * @author Michael Bädorf
 */
@Mapper(componentModel = "jsr330")
public abstract class JPAScreeningSheetServiceRepositoryMapper {

    @Setter
    private ObjectMapper mapper;

    /**
     * Erstelluing eines neuen Domänen-Objektes mit den Daten der Entität.<p>
     *
     * @param entity Quelle
     * @return Neu erestelltes Domänen-Objekt
     */
    @Mapping(target = "value", ignore = true)
    public abstract Parameter toDomain(ParameterEntity entity);

    @SneakyThrows
    @AfterMapping
    void toDomain(ParameterEntity entity, @MappingTarget ParameterBuilder domain) {
        if (SKALAR == entity.getParameterType()) {
            domain.value(valueOf(entity.getValue()));
        } else if (MATRIX == entity.getParameterType()) {
            domain.value(mapper.readValue(entity.getValue(), double[][].class));
        } else if(STRING == entity.getParameterType()) {
            domain.value(entity.getValue());
        }
    }
}
