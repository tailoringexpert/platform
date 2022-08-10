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
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelektionsVektor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;

@RequiredArgsConstructor
public class ScreeningSheetServiceImpl implements ScreeningSheetService {

    @NonNull
    private ScreeningSheetServiceMapper mapper;

    @NonNull
    private ScreeningSheetServiceRepository repository;

    @NonNull
    private ScreeningSheetParameterProvider screeningSheetParameterProvider;

    @NonNull
    private SelektionsVektorProvider selektionsVectorProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public SelektionsVektor berechneSelektionsVektor(@NonNull byte[] rawData) {
        Collection<ScreeningSheetParameterEintrag> screeningSheetEingabeParameter = screeningSheetParameterProvider.parse(new ByteArrayInputStream(rawData));
        Collection<Parameter> parameterKonfigurationsWerte = getParameter(screeningSheetEingabeParameter);

        return selektionsVectorProvider.apply(parameterKonfigurationsWerte);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreeningSheet createScreeningSheet(@NonNull byte[] rawData) {
        Collection<ScreeningSheetParameterEintrag> screeningSheetEingabeParameter = screeningSheetParameterProvider.parse(new ByteArrayInputStream(rawData));
        Collection<Parameter> parameterKonfigurationsWerte = getParameter(screeningSheetEingabeParameter);

        List<ScreeningSheetParameter> screeningSheetParameter = parameterKonfigurationsWerte
            .stream()
            .map(mapper::createScreeningSheet)
            .collect(toList());

        // phasen konvertieren und an liste hinzufügen
        List<Phase> phase = screeningSheetEingabeParameter
            .stream()
            .filter(entry -> "phase".equalsIgnoreCase(entry.getKategorie()))
            .map(entry -> Phase.fromString(entry.getName()))
            .filter(Objects::nonNull)
            .sorted(comparing(Phase::ordinal))
            .collect(toCollection(LinkedList::new));
        if (!phase.isEmpty()) {
            screeningSheetParameter.add(ScreeningSheetParameter.builder()
                .bezeichnung("Phase")
                .wert(phase)
                .build());
        }

        List<String> parameterKonfigurationsOhneWerte = parameterKonfigurationsWerte
            .stream()
            .map(Parameter::getName)
            .collect(Collectors.toUnmodifiableList());

//         Parameter, die nicht aus der DB stammen, da sie keinen Einfluss auf Berechnung Selektionsvektor haben
        screeningSheetParameter.addAll(screeningSheetEingabeParameter
            .stream()
            .filter(entry -> !"phase".equalsIgnoreCase(entry.getKategorie()) && !parameterKonfigurationsOhneWerte.contains(entry.getName()))
            .map(entry -> ScreeningSheetParameter.builder()
                .bezeichnung(entry.getName().substring(0, 1).toUpperCase(Locale.GERMANY) + entry.getName().substring(1))
                .wert(entry.getBezeichnung())
                .build())
            .collect(toList()));

        SelektionsVektor selektionsVektor = selektionsVectorProvider.apply(parameterKonfigurationsWerte);

        return ScreeningSheet.builder()
            .data(rawData)
            .parameters(screeningSheetParameter)
            .selektionsVektor(selektionsVektor)
            .build();
    }

    private Collection<Parameter> getParameter(Collection<ScreeningSheetParameterEintrag> screeningSheetEingabeParameter) {
        Set<String> parameterNamen = screeningSheetEingabeParameter
            .stream()
            .map(ScreeningSheetParameterEintrag::getName)
            .collect(toUnmodifiableSet());
        return repository.getParameter(parameterNamen);
    }
}
