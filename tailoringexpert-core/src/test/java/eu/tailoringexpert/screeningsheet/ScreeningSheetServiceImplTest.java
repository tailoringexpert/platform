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
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelektionsVektor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    SelektionsVektorProvider selektionsVectorProviderMock;
    ScreeningSheetServiceImpl service;

    @BeforeEach
    void setup() {
        this.mapperMock = mock(ScreeningSheetServiceMapper.class);
        this.repositoryMock = mock(ScreeningSheetServiceRepository.class);
        this.screeningDataProviderMock = mock(ScreeningSheetParameterProvider.class);
        this.selektionsVectorProviderMock = mock(SelektionsVektorProvider.class);
        this.service = new ScreeningSheetServiceImpl(
            mapperMock,
            repositoryMock,
            screeningDataProviderMock,
            selektionsVectorProviderMock
        );
    }

    @Test
    void berechneSelektionsVektor_KeineDaten_NullPointerWirdGeworfen() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.berechneSelektionsVektor(null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void berechneSelektionsVektor_DatenVorhanden_SelektionsvektorWirdBerechnet() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        List<ScreeningSheetParameterEintrag> screeningSheetParameters = List.of(
            ScreeningSheetParameterEintrag.builder()
                .kategorie("Produkttyp")
                .name("SAT")
                .bezeichnung("Satellite")
                .build(),
            ScreeningSheetParameterEintrag.builder()
                .kategorie("Einsatzzweck")
                .name("Erdbeobachtung")
                .bezeichnung("Erdbeobachtung")
                .build(),
            ScreeningSheetParameterEintrag.builder()
                .kategorie("Anwendungscharakter")
                .name("wissenschaftlich")
                .bezeichnung("wissenschaftliche Anwendung")
                .build(),
            ScreeningSheetParameterEintrag.builder()
                .kategorie("Lebensdauer")
                .name("Dauer1")
                .bezeichnung("t < 2 Jahre")
                .build(),
            ScreeningSheetParameterEintrag.builder()
                .kategorie("Programmatische Bewertung")
                .name("Programmatik1")
                .bezeichnung("erforderlich")
                .build(),
            ScreeningSheetParameterEintrag.builder()
                .kategorie("Kosten/Budget")
                .name("Budget1")
                .bezeichnung("<2 Mio")
                .build());

        given(screeningDataProviderMock.parse(any(ByteArrayInputStream.class)))
            .willReturn(screeningSheetParameters);


        ArgumentCaptor<Set<String>> parameterCaptor = ArgumentCaptor.forClass(Set.class);
        Collection<Parameter> parameter = screeningSheetParameters
            .stream()
            .map(ScreeningSheetParameterEintrag::getName)
            .map(name -> Parameter.builder().name(name).build())
            .collect(toUnmodifiableSet());
        given(repositoryMock.getParameter(parameterCaptor.capture()))
            .willReturn(parameter);

        ArgumentCaptor<Collection<Parameter>> selektionsVektorCaptor = ArgumentCaptor.forClass(Collection.class);
        given(selektionsVectorProviderMock.apply(selektionsVektorCaptor.capture()))
            .willReturn(SelektionsVektor.builder().build());

        // act
        SelektionsVektor actual = service.berechneSelektionsVektor(data);

        // assert
        assertThat(actual).isNotNull();

        assertThat(parameterCaptor.getValue()).containsAll(screeningSheetParameters.stream()
            .map(ScreeningSheetParameterEintrag::getName)
            .collect(toSet())
        );

        assertThat(selektionsVektorCaptor.getValue()).containsAll(parameter);
        verify(selektionsVectorProviderMock, times(1))
            .apply(parameter);

    }

    @Test
    void createScreeningSheet_DatenVorhanden_ScreeningSheetWirdErstellt() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        given(screeningDataProviderMock.parse(any()))
            .willReturn(new ArrayList<>());

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
    void createScreeningSheet_DatenVorhanden_ScreeningSheetWirdErstellt1() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }
        List<ScreeningSheetParameterEintrag> screeningSheetParameters = List.of(
            ScreeningSheetParameterEintrag.builder()
                .kategorie("Produkttyp")
                .name("SAT")
                .bezeichnung("Satellit")
                .build(),
            ScreeningSheetParameterEintrag.builder()
                .kategorie("Projekt")
                .name("Kuerzel")
                .bezeichnung("DUMMY")
                .build()
        );
        given(screeningDataProviderMock.parse(any())).willReturn(screeningSheetParameters);

        Collection<Parameter> parameter = Arrays.asList(
            Parameter.builder().kategorie("Produkttyp").name("SAT").build()
        );
        given(repositoryMock.getParameter(anyCollection())).willReturn(parameter);

        given(mapperMock.createScreeningSheet(any())).willAnswer(invocation -> {
            Parameter p = (Parameter) invocation.getArgument(0);
            return ScreeningSheetParameter.builder().bezeichnung(p.getName()).build();
        });

        // act
        ScreeningSheet actual = service.createScreeningSheet(data);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getData()).isEqualTo(data);
        assertThat(actual.getParameters()).hasSize(2);
        verify(repositoryMock, times(1)).getParameter(anyCollection());
    }

    @Test
    void createScreeningSheet_KeineDatenVorhanden_NullPointerExceptionWirdGeworfen() throws IOException {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> service.createScreeningSheet(null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
        verify(repositoryMock, times(0)).getParameter(anyCollection());
    }


}
