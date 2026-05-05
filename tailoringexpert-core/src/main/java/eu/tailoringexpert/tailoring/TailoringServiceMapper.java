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

import static java.util.Collections.disjoint;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toUnmodifiableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueMappingStrategy;

import eu.tailoringexpert.TailoringexpertMapperConfig;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringInformation;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringRequirement.TailoringRequirementBuilder;
import lombok.extern.log4j.Log4j2;

/**
 * Mapper for converting data object in @see {@link TailoringService}.
 *
 * @author Michael Bädorf
 */
@Log4j2
@Mapper(config = TailoringexpertMapperConfig.class, nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
@SuppressWarnings("java:S1610")
public abstract class TailoringServiceMapper {

    @Mapping(target = "catalogVersion", source = "domain.catalog.version")
    abstract TailoringInformation toTailoringInformation(Tailoring domain);

    abstract Catalog<TailoringRequirement> toTailoringCatalog(
            Catalog<BaseRequirement> catalog,
            @Context ScreeningSheet screeningSheet,
            @Context SelectionVector selectionVector,
            @Context Optional<Map<String, Collection<ImportRequirement>>> matrixRequirements);

    @Mapping(target = "requirements", expression = "java(toTailoringRequirements(chapter.getRequirements(), screeningSheet, selectionVector, chapter, matrixRequirements))")
    abstract Chapter<TailoringRequirement> toChapter(
            Chapter<BaseRequirement> chapter,
            @Context ScreeningSheet screeningSheet,
            @Context SelectionVector selectionVector,
            @Context Optional<Map<String, Collection<ImportRequirement>>> matrixRequirements);

    abstract ArrayList<TailoringRequirement> toTailoringRequirements(
            List<BaseRequirement> requirements,
            @Context ScreeningSheet screeningSheet,
            @Context SelectionVector selectionVector,
            @Context Chapter<BaseRequirement> chapter,
            @Context Optional<Map<String, Collection<ImportRequirement>>> matrixRequirements);

    /**
     * Function to set selected state of a requirement after value mapping has
     * performed/finished.
     *
     * @param baseRequirement base requirement containing rules for selecting in
     *                        automatic tailoring
     * @param screeningSheet  screeningsheet to use
     * @param selectionVector selectionvector to use for selecting the requirement
     * @param builder         data object to set selected state
     */
    @AfterMapping
    void toTailoringRequirement(
            BaseRequirement baseRequirement,
            @Context ScreeningSheet screeningSheet,
            @Context SelectionVector selectionVector,
            @Context Chapter<BaseRequirement> chapter,
            @Context Optional<Map<String, Collection<ImportRequirement>>> matrixRequirements,
            @MappingTarget TailoringRequirementBuilder builder) {
        log.traceEntry(baseRequirement::getPosition);

        matrixRequirements.orElseGet(() -> emptyMap()).getOrDefault(chapter.getNumber(), emptyList())
                .stream()
                .filter(req -> req.getPosition().equals(baseRequirement.getPosition()))
                .findFirst()
                .ifPresentOrElse(
                        requirement -> builder.selected("YES".equals(requirement.getApplicable())),
                        () -> builder
                                .selected(tailoringRequiremenState(baseRequirement, screeningSheet, selectionVector)));
    }

    private Boolean tailoringRequiremenState(
            BaseRequirement baseRequirement,
            ScreeningSheet screeningSheet,
            SelectionVector selectionVector) {
        log.traceEntry(baseRequirement::getPosition);

        Set<String> parameterValues = screeningSheet.getParameters()
                .stream()
                .map(ScreeningSheetParameter::getName)
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(toUnmodifiableSet());

        Collection<Phase> phases = screeningSheet.getPhases();
        boolean isRelevantPhase = containsPhases(phases, baseRequirement.getPhases());
        boolean applicable = baseRequirement.getIdentifiers()
                .stream()
                .filter(identifier -> isRelevantPhase)
                .anyMatch(identifier -> {
                    int level = selectionVector.getLevel(identifier.getType());
                    // prüfen, ob abwendbarkeit ohne einschränkung
                    if (!identifier.hasLimitations() && level >= identifier.getLevel()) {
                        return true;
                    }

                    // sind alle limitierungen enthalten und ist der level gleich
                    return containsAllLimitations(parameterValues, identifier.getLimitations())
                            && level == identifier.getLevel();
                });

        return log.traceExit(applicable);
    }

    /**
     * Check, if phases of screening parameters exists in base catalog requirment
     * phases.
     *
     * @param screeningPhases   phases provided in screeningsheet paranmeters
     * @param requirementPhases phase defined in base catalog requirement
     * @return
     */
    private boolean containsPhases(
            Collection<Phase> screeningPhases,
            Collection<Phase> requirementPhases) {
        return isNull(requirementPhases) || requirementPhases.isEmpty() ||
                !disjoint(requirementPhases, screeningPhases);
    }

    /**
     * Chek, if all limitation definined in requirement are fulfilled.
     *
     * @param screeningParameter
     * @param requirement
     * @return
     */
    private boolean containsAllLimitations(Collection<String> screeningParameter, Collection<String> requirement) {
        return screeningParameter.containsAll(requirement);
    }

}
