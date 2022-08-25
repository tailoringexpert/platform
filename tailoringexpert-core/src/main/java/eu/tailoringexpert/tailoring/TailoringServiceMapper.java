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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringRequirement.TailoringRequirementBuilder;
import eu.tailoringexpert.domain.TailoringInformation;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collection;
import java.util.Set;

import static eu.tailoringexpert.domain.ScreeningSheet.PHASE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.disjoint;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Mapper
@SuppressWarnings("java:S1610")
public abstract class TailoringServiceMapper {

    /**
     * Erstellt ein neues Domänen-Objekt mit den Werten der Phasen.
     *
     * @param domain Quelle
     * @return Das erstellte Domänen-Objekt
     */
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "domain.name")
    @Mapping(target = "phases", source = "domain.phases")
    abstract TailoringInformation toTailoringInformation(Tailoring domain);

    /**
     * Konvertiert einen übergebenen Anforderungskatalog in einen projektspezifischen Anforderungskatalog.
     *
     * @param catalog         Der zu konvertierende Catalog
     * @param screeningSheet  ScreeningSheet mit den Informationen für die Anwendbarkeit von Anforderungen
     * @param selectionVector Selektionsvektor für die Anwendbarkeit von Anforderungen
     * @return konvertierter Projektkatalog
     */
    abstract Catalog<TailoringRequirement> toTailoringCatalog(
        Catalog<BaseRequirement> catalog,
        @Context ScreeningSheet screeningSheet,
        @Context SelectionVector selectionVector
    );

    @AfterMapping
    void toTailoringRequirement(
        BaseRequirement baseRequirement,
        @Context ScreeningSheet screeningSheet,
        @Context SelectionVector selectionVector,
        @MappingTarget TailoringRequirementBuilder builder) {
        Set<String> parameterValues = screeningSheet.getParameters()
            .stream()
            .map(ScreeningSheetParameter::getValue)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .collect(toUnmodifiableSet());

        Collection<Phase> phases = filterPhases(screeningSheet.getParameters());
        boolean isRelevantPhase = containsScreeningPhase(phases, baseRequirement.getPhases());
        baseRequirement.getIdentifiers()
            .stream()
            .filter(identifier -> isRelevantPhase)
            .filter(identifier -> {
                int level = selectionVector.getLevel(identifier.getType());
                // prüfen, ob abwendbarkeit ohne einschränkung
                if ((isNull(identifier.getLimitations()) || identifier.getLimitations().isEmpty()) &&
                    level >= identifier.getLevel()) {
                    return true;
                }

                // sind alle limitierungen enthalten und ist der level gleich
                return containsAllLimitations(parameterValues, identifier.getLimitations()) && level == identifier.getLevel();
            })
            .findFirst()
            .ifPresentOrElse(applicability -> builder.selected(TRUE), () -> builder.selected(FALSE));
    }

    private Collection<Phase> filterPhases(Collection<ScreeningSheetParameter> parameters) {
        return (Collection<Phase>) parameters
            .stream()
            .filter(parameter -> PHASE.equalsIgnoreCase(parameter.getCategory()))
            .findFirst()
            .orElseThrow(RuntimeException::new)
            .getValue();
    }

    private boolean containsScreeningPhase(
        Collection<Phase> screeningPhases,
        Collection<Phase> requirementPhases) {
        return isNull(requirementPhases) || requirementPhases.isEmpty() ||
            !disjoint(requirementPhases, screeningPhases);
    }

    private boolean containsAllLimitations(Collection<String> screeningParameter, Collection<String> requirements) {
        return screeningParameter.containsAll(requirements);
    }

}
