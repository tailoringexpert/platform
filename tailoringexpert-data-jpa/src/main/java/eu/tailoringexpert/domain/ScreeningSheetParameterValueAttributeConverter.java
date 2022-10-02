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
package eu.tailoringexpert.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import java.util.Collection;

public class ScreeningSheetParameterValueAttributeConverter implements AttributeConverter<Object, String> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(Object attribute) {
        return mapper.writeValueAsString(attribute);
    }

    @Override
    @SneakyThrows
    public Object convertToEntityAttribute(String dbData) {
        Class<?> clz = mapper.readTree(dbData).isArray() ? Collection.class : String.class;
        return mapper.readValue(dbData, clz);
    }
}

