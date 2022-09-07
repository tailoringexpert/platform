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
package eu.tailoringexpert.screeingsheet;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.tailoringexpert.domain.DatenType;
import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.ParameterEntity;
import eu.tailoringexpert.screeningsheet.JPAScreeningSheetServiceRepositoryMapper;
import eu.tailoringexpert.screeningsheet.JPAScreeningSheetServiceRepositoryMapperImpl;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class JPAScreeningSheetServiceRepositoryMapperTest {

    private JPAScreeningSheetServiceRepositoryMapper mapper;

    @BeforeEach
    void setup() {
        this.mapper = new JPAScreeningSheetServiceRepositoryMapperImpl();
        this.mapper.setMapper(new ObjectMapper());
    }

    @Test
    void toDomain_MatrixParameterType_DoubleArrayValueReturned() {
        // arrange
        ParameterEntity entity = ParameterEntity.builder()
            .category("Einsatzort")
            .name("LEO")
            .parameterType(DatenType.MATRIX)
            .value("[[0.8,0,0,0,0,0,0,0,0,0],[0,0.8,0,0,0,0,0,0,0,0],[0,0,1,0,0,0,0,0,0,0],[0,0,0,1,0,0,0,0,0,0],[0,0,0,0,0.9,0,0,0,0,0],[0,0,0,0,0,1,0,0,0,0],[0,0,0,0,0,0,1,0,0,0],[0,0,0,0,0,0,0,1,0,0],[0,0,0,0,0,0,0,0,0,0],[0,0,0,0,0,0,0,0,0,0.95]]")
            .build();

        // act
        Parameter actual = mapper.toDomain(entity);

        // assert
        assertThat(actual.getValue()).isInstanceOf(double[][].class);
        assertThat((double[][]) actual.getValue()).hasDimensions(10, 10);
    }

    @Test
    void toDomain_ScalarParameterTyep_IntegerValueReturned() {
        // arrange
        ParameterEntity entity = ParameterEntity.builder()
            .category("Einsatzort")
            .name("LEO")
            .parameterType(DatenType.SKALAR)
            .value("1")
            .build();

        // act
        Parameter actual = mapper.toDomain(entity);

        // assert
        assertThat(actual.getValue()).isInstanceOf(Integer.class);
        assertThat((Integer) actual.getValue()).isEqualTo(1);

    }
}
