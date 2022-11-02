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

import eu.tailoringexpert.TailoringexpertException;
import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelectionVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ScreeningSheetServiceImplTest {

    ScreeningSheetServiceMapper mapperMock;
    ScreeningSheetServiceRepository repositoryMock;
    ScreeningSheetParameterProvider screeningDataProviderMock;
    SelectionVectorProvider selektionsVectorProviderMock;
    ScreeningSheetServiceImpl service;

    @BeforeEach
    void setup() {
        this.mapperMock = mock(ScreeningSheetServiceMapper.class);
        this.repositoryMock = mock(ScreeningSheetServiceRepository.class);
        this.screeningDataProviderMock = mock(ScreeningSheetParameterProvider.class);
        this.selektionsVectorProviderMock = mock(SelectionVectorProvider.class);
        this.service = new ScreeningSheetServiceImpl(
            mapperMock,
            repositoryMock,
            screeningDataProviderMock,
            selektionsVectorProviderMock
        );
    }

    @Test
    void calculateSelectionVector_NullData_NullPointerThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.calculateSelectionVector(null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void calculateSelectionVector_DataExists_SelectionvectorCalculated() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        List<ScreeningSheetParameterField> screeningSheetParameters = List.of(
            ScreeningSheetParameterField.builder()
                .category("Produkttyp")
                .name("SAT")
                .label("Satellite")
                .build(),
            ScreeningSheetParameterField.builder()
                .category("Einsatzzweck")
                .name("Erdbeobachtung")
                .label("Erdbeobachtung")
                .build(),
            ScreeningSheetParameterField.builder()
                .category("Anwendungscharakter")
                .name("wissenschaftlich")
                .label("wissenschaftliche Anwendung")
                .build(),
            ScreeningSheetParameterField.builder()
                .category("Lebensdauer")
                .name("Dauer1")
                .label("t < 2 Jahre")
                .build(),
            ScreeningSheetParameterField.builder()
                .category("Programmatische Bewertung")
                .name("Programmatik1")
                .label("erforderlich")
                .build(),
            ScreeningSheetParameterField.builder()
                .category("Kosten/Budget")
                .name("Budget1")
                .label("<2 Mio")
                .build());

        given(screeningDataProviderMock.parse(any(ByteArrayInputStream.class)))
            .willReturn(screeningSheetParameters);


        ArgumentCaptor<Set<String>> parameterCaptor = ArgumentCaptor.forClass(Set.class);
        Collection<Parameter> parameter = screeningSheetParameters
            .stream()
            .map(ScreeningSheetParameterField::getName)
            .map(name -> Parameter.builder().name(name).build())
            .collect(toUnmodifiableSet());
        given(repositoryMock.getParameter(parameterCaptor.capture()))
            .willReturn(parameter);

        ArgumentCaptor<Collection<Parameter>> selektionsVektorCaptor = ArgumentCaptor.forClass(Collection.class);
        given(selektionsVectorProviderMock.apply(selektionsVektorCaptor.capture()))
            .willReturn(SelectionVector.builder().build());

        // act
        SelectionVector actual = service.calculateSelectionVector(data);

        // assert
        assertThat(actual).isNotNull();

        assertThat(parameterCaptor.getValue()).containsAll(screeningSheetParameters.stream()
            .map(ScreeningSheetParameterField::getName)
            .collect(toSet())
        );

        assertThat(selektionsVektorCaptor.getValue()).containsAll(parameter);
        verify(selektionsVectorProviderMock, times(1))
            .apply(parameter);

    }

    @Test
    void createScreeningSheet_DataExists_ScreeningSheetCreated() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(screeningDataProviderMock.parse(any()))
            .willReturn(List.of(
                ScreeningSheetParameterField.builder()
                    .category("Project")
                    .name(ScreeningSheet.PROJECT)
                    .label("Sample")
                    .build()
            ));

        given(repositoryMock.getParameter(anyCollection()))
            .willReturn(Collections.emptyList());

        // act
        ScreeningSheet actual = service.createScreeningSheet(data);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isEqualTo(data);
        verify(repositoryMock, times(1)).getParameter(anyCollection());
    }

    @Test
    void createScreeningSheet_DataExists_ScreeningSheetWirdErstellt1() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }
        List<ScreeningSheetParameterField> screeningSheetParameters = List.of(
            ScreeningSheetParameterField.builder()
                .category("Project")
                .name(ScreeningSheet.PROJECT)
                .label("Sample")
                .build(),
            ScreeningSheetParameterField.builder()
                .category("Produkttyp")
                .name("SAT")
                .label("Satellit")
                .build(),
            ScreeningSheetParameterField.builder()
                .category("Project")
                .name("Identifier")
                .label("DUMMY")
                .build(),
            ScreeningSheetParameterField.builder()
                .category(ScreeningSheet.PHASE)
                .name("A")
                .build(),
            ScreeningSheetParameterField.builder()
                .category(ScreeningSheet.PHASE)
                .name("ZERO")
                .build()
        );

        given(screeningDataProviderMock.parse(any())).willReturn(screeningSheetParameters);

        Collection<Parameter> parameter = Arrays.asList(
            Parameter.builder().category("Produkttyp").name("SAT").build()
        );
        given(repositoryMock.getParameter(anyCollection())).willReturn(parameter);

        given(mapperMock.createScreeningSheet(any())).willAnswer(invocation -> {
            Parameter p = (Parameter) invocation.getArgument(0);
            return ScreeningSheetParameter.builder().category(p.getName()).build();
        });

        // act
        ScreeningSheet actual = service.createScreeningSheet(data);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isEqualTo(data);
        assertThat(actual.getParameters()).hasSize(4);
        verify(repositoryMock, times(1)).getParameter(anyCollection());
    }

    @Test
    void createScreeningSheet_DataNotExists_NullPointerExceptionThrown() throws IOException {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.createScreeningSheet(null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
        verify(repositoryMock, times(0)).getParameter(anyCollection());
    }

    @Test
    void createScreeningSheet_ProjectNull_TailoringexpertExceptionThrown() {
        // arrange

        given(screeningDataProviderMock.parse(any())).willReturn(Collections.emptyList());

        // act
        Throwable actual = catchThrowable(() -> service.createScreeningSheet(new byte[0]));

        // assert
        assertThat(actual).isInstanceOf(TailoringexpertException.class);
        verify(repositoryMock, times(0)).getParameter(anyCollection());
    }

    @Test
    void createScreeningSheet_ScreeningSheetParameterProjectNullLabel_TailoringexpertExceptionThrown() throws Exception {
        // arrange
        given(screeningDataProviderMock.parse(any())).willReturn(
            List.of(
                ScreeningSheetParameterField.builder()
                    .category("Project")
                    .name(ScreeningSheet.PROJECT)
                    .label(null)
                    .build()
            )
        );

        // act
        Throwable actual = catchThrowable(() -> service.createScreeningSheet(new byte[0]));

        // assert
        assertThat(actual).isInstanceOf(TailoringexpertException.class);
        verify(repositoryMock, times(0)).getParameter(anyCollection());
    }

    @Test
    void createScreeningSheet_ScreeningSheetParameterProjectEmptyLabel_TailoringexpertExceptionThrown() throws Exception {
        // arrange
        given(screeningDataProviderMock.parse(any())).willReturn(
            List.of(
                ScreeningSheetParameterField.builder()
                    .category("Project")
                    .name(ScreeningSheet.PROJECT)
                    .label("")
                    .build()
            )
        );

        // act
        Throwable actual = catchThrowable(() -> service.createScreeningSheet(new byte[0]));

        // assert
        assertThat(actual).isInstanceOf(TailoringexpertException.class);
        verify(repositoryMock, times(0)).getParameter(anyCollection());
    }
}
