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
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelectionVector;
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
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * Implementation of {@link ScreeningSheetService}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class ScreeningSheetServiceImpl implements ScreeningSheetService {

    @NonNull
    private ScreeningSheetServiceMapper mapper;

    @NonNull
    private ScreeningSheetServiceRepository repository;

    @NonNull
    private ScreeningSheetParameterProvider screeningSheetParameterProvider;

    @NonNull
    private SelectionVectorProvider selectionVectorProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public SelectionVector calculateSelectionVector(@NonNull byte[] rawData) {
        Collection<ScreeningSheetParameterField> screeningSheetParameters = screeningSheetParameterProvider.parse(new ByteArrayInputStream(rawData));
        Collection<Parameter> parameters = getParameter(screeningSheetParameters);

        return selectionVectorProvider.apply(parameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreeningSheet createScreeningSheet(@NonNull byte[] rawData) {
        Collection<ScreeningSheetParameterField> screeningSheetParameters = screeningSheetParameterProvider.parse(new ByteArrayInputStream(rawData));

        String project = screeningSheetParameters.stream()
            .filter(parameter -> ScreeningSheet.PARAMETER_PROJECT.equalsIgnoreCase(parameter.getName()))
            .findFirst()
            .map(ScreeningSheetParameterField::getLabel)
            .filter(Objects::nonNull)
            .filter(not(String::isEmpty))
            .orElseThrow(() -> new TailoringexpertException("Screeningsheet doesn't contain a project name!"));

        Collection<Parameter> parameters = getParameter(screeningSheetParameters);

        List<ScreeningSheetParameter> screeningSheetParameter = parameters
            .stream()
            .map(mapper::createScreeningSheet)
            .collect(toList());

        // phasen konvertieren und an liste hinzufügen
        List<Phase> phases = screeningSheetParameters
            .stream()
            .filter(entry -> ScreeningSheet.PARAMETER_PHASE.equalsIgnoreCase(entry.getCategory()))
            .map(entry -> Phase.fromString(entry.getName()))
            .filter(Objects::nonNull)
            .sorted(comparing(Phase::ordinal))
            .collect(toCollection(LinkedList::new));
        if (!phases.isEmpty()) {
            screeningSheetParameter.add(ScreeningSheetParameter.builder()
                .category(ScreeningSheet.PARAMETER_PHASE.substring(0, 1).toUpperCase(Locale.ROOT) + ScreeningSheet.PARAMETER_PHASE.substring(1))
                .value(phases)
                .build());
        }

        List<String> parameterConfigurationWithoutValues = parameters
            .stream()
            .map(Parameter::getName)
            .collect(Collectors.toUnmodifiableList());


        // all parameter not defined in db, which have no effect on calculating a selectionvector
        screeningSheetParameter.addAll(screeningSheetParameters
            .stream()
            .filter(entry -> !ScreeningSheet.PARAMETER_PHASE.equalsIgnoreCase(entry.getCategory()) && !parameterConfigurationWithoutValues.contains(entry.getName()))
            .map(entry -> ScreeningSheetParameter.builder()
                .category(entry.getName().substring(0, 1).toUpperCase(Locale.GERMANY) + entry.getName().substring(1))
                .value(entry.getLabel())
                .build())
            .collect(toList()));

        SelectionVector selectionVector = selectionVectorProvider.apply(parameters);

        return ScreeningSheet.builder()
            .project(project)
            .data(rawData)
            .phases(phases)
            .parameters(screeningSheetParameter)
            .selectionVector(selectionVector)
            .build();
    }

    private Collection<Parameter> getParameter(Collection<ScreeningSheetParameterField> screeningSheetParameters) {
        Set<String> parameterNames = screeningSheetParameters
            .stream()
            .map(ScreeningSheetParameterField::getName)
            .collect(toUnmodifiableSet());
        return repository.getParameter(parameterNames);
    }
}
