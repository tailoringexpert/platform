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
package de.baedorf.tailoringexpert.tailoring;

import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import de.baedorf.tailoringexpert.domain.Phase;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetParameter;
import de.baedorf.tailoringexpert.domain.SelektionsVektor;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung.TailoringAnforderungBuilder;
import de.baedorf.tailoringexpert.domain.TailoringInformation;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collection;
import java.util.Set;

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
    @Mapping(target = "phasen", source = "domain.phasen")
    abstract TailoringInformation toTailoringInformation(Tailoring domain);

    /**
     * Konvertiert einen übergebenen Anforderungskatalog in einen projektspezifischen Anforderungskatalog.
     *
     * @param katalog          Der zu konvertierende Katalog
     * @param screeningSheet   ScreeningSheet mit den Informationen für die Anwendbarkeit von Anforderungen
     * @param selektionsVektor Selektionsvektor für die Anwendbarkeit von Anforderungen
     * @return konvertierter Projektkatalog
     */
    abstract Katalog<TailoringAnforderung> toTailoringKatalog(
        Katalog<KatalogAnforderung> katalog,
        @Context ScreeningSheet screeningSheet,
        @Context SelektionsVektor selektionsVektor
    );

    @AfterMapping
    void toTailoringAnforderung(
        KatalogAnforderung katalogAnforderung,
        @Context ScreeningSheet screeningSheet,
        @Context SelektionsVektor selektionsVektor,
        @MappingTarget TailoringAnforderungBuilder builder) {

        Collection<Phase> phasen = (Collection<Phase>) screeningSheet.getParameters()
            .stream()
            .filter(parameter -> "phase".equalsIgnoreCase(parameter.getBezeichnung()))
            .findFirst()
            .orElseThrow(RuntimeException::new)
            .getWert();

        boolean isRelevantPhase = containsScreeningPhase(
            phasen,
            katalogAnforderung.getPhasen()
        );

        Set<String> parameterValues = screeningSheet.getParameters()
            .stream()
            .map(ScreeningSheetParameter::getWert)
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .collect(toUnmodifiableSet());

        katalogAnforderung.getIdentifikatoren()
            .stream()
            .filter(applicability -> isRelevantPhase)
            .filter(applicability -> {
                int level = selektionsVektor.getLevel(applicability.getTyp());
                // prüfen, ob abwendbarkeit ohne einschränkung
                if ((isNull(applicability.getLimitierungen()) || applicability.getLimitierungen().isEmpty()) &&
                    level >= applicability.getLevel()) {
                    return true;
                }

                // sind alle limitierungen enthalten und ist der level gleich
                return containsAllLimitations(parameterValues, applicability.getLimitierungen()) && level == applicability.getLevel();
            })
            .findFirst()
            .ifPresentOrElse(applicability -> builder.ausgewaehlt(TRUE), () -> builder.ausgewaehlt(FALSE));
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
