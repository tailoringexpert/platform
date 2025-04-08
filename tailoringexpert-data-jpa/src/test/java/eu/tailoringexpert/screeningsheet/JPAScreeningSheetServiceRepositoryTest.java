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

import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.ParameterEntity;
import eu.tailoringexpert.repository.ParameterRepository;
import eu.tailoringexpert.repository.SelectionVectorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class JPAScreeningSheetServiceRepositoryTest {

    JPAScreeningSheetServiceRepositoryMapper mapperMock;
    ParameterRepository parameterRepositoryMock;
    SelectionVectorProfileRepository selectionVectorProfileRepositoryMock;
    JPAScreeningSheetServiceRepository repository;

    @BeforeEach
    void setup() {
        this.mapperMock = mock(JPAScreeningSheetServiceRepositoryMapper.class);
        this.parameterRepositoryMock = mock(ParameterRepository.class);
        this.repository = new JPAScreeningSheetServiceRepository(
            mapperMock,
            parameterRepositoryMock
        );
    }

    @Test
    void getParameter_3ParameterNames_ParameterListReturned() {
        // arrange
        Collection<String> namen = Arrays.asList("param1", "param2", "param3");

        given(parameterRepositoryMock.findByNameIn(namen))
            .willAnswer(invocation ->
                ((Collection<String>) invocation.getArgument(0))
                    .stream()
                    .map(name -> ParameterEntity.builder().name(name).build())
                    .toList()
            );

        // act
        Collection<Parameter> actual = repository.getParameter(namen);

        // assert
        assertThat(actual).hasSize(3);
        verify(mapperMock, times(3)).toDomain(any(ParameterEntity.class));
    }
}
