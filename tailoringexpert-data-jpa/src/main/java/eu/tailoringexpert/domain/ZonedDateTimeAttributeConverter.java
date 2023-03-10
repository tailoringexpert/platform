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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.ZonedDateTime;

import static java.time.ZonedDateTime.parse;
import static java.util.Objects.nonNull;

/**
 * Registriert {@link ZonedDateTime} für diesen Konverrter.
 */
@Converter(autoApply = true)
public class ZonedDateTimeAttributeConverter implements AttributeConverter<ZonedDateTime, String> {

    @Override
    public String convertToDatabaseColumn(ZonedDateTime attribute) {
        return nonNull(attribute) ? attribute.toString() : null;
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(String dbData) {
        return nonNull(dbData) ? parse(dbData) : null;
    }
}
