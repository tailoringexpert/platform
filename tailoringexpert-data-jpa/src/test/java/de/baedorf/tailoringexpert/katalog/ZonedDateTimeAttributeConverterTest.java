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
package de.baedorf.tailoringexpert.katalog;

import de.baedorf.tailoringexpert.domain.ZonedDateTimeAttributeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ZonedDateTimeAttributeConverterTest {

    ZonedDateTimeAttributeConverter converter;

    @BeforeEach
    void beforeEach() {
        this.converter = new ZonedDateTimeAttributeConverter();
    }

    @Test
    void convertToEntityAttribute_InputNull_NullReturned() {
        // arrange
        String dbData = null;

        // act
        ZonedDateTime actual = converter.convertToEntityAttribute(dbData);

        // assert
        assertThat(actual).isNull();
    }


    @Test
    void convertToEntityAttribute_InputNonNull_ZonedDateTimeRepresentationReturned() {
        // arrange
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String dbData = zonedDateTime.toString();

        // act
        ZonedDateTime actual = converter.convertToEntityAttribute(dbData);

        // assert
        assertThat(actual).isEqualTo(zonedDateTime);
    }

    @Test
    void convertToDatabaseColumn_InputNull_NullReturned() {
        // arrange
        ZonedDateTime dbData = null;

        // act
        String actual = converter.convertToDatabaseColumn(dbData);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void convertToDatabaseColumn_InputNonNull_StringReturned() {
        // arrange
        ZonedDateTime zonedDateTime = ZonedDateTime.now();

        // act
        String actual = converter.convertToDatabaseColumn(zonedDateTime);

        // assert
        assertThat(actual).isEqualTo(zonedDateTime.toString());
    }

}
