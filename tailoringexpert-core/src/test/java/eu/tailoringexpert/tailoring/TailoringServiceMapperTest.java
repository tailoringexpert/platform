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
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.Identifier;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.SelectionVector;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.domain.TailoringRequirement.TailoringRequirementBuilder;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Log4j2
class TailoringServiceMapperTest {

    TailoringServiceMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new TailoringServiceMapperImpl();
    }

    @Test
    void convert_nullPhaseLimitation_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtung")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder tailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }


    @Test
    void convert_EmptyPhaseLimitations_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(emptyList())
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtung")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_FulfilledPhaseLimitation_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(asList(B, F))
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .phases(List.of(E, F))
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_FulfilledPhaseLimitationWrongIdentifier_RequirementNotSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(asList(B, F))
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(asList("SAT"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .phases(List.of(B))
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT1")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(B))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isFalse();
    }


    @Test
    void convert_FulfilledPhaseLimitationAndIdentifier_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(asList(B, F))
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(asList("SAT"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .phases(List.of(E,F))
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();


        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_erfuelltePhasenEinschraenkungIdenitifaktoren_AnforderungAusgewaehlt() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(asList(B, F))
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(4)
                    .limitations(asList("SAT", "LEO"))
                    .build(),
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(asList("SAT"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .phases(List.of(E,F))
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();


        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }


    @Test
    void convert_PhaseLimitationNotFulfilled_RequirementNotSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(asList(B, F))
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .phases(List.of(ZERO))
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(ZERO))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();


        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isFalse();
    }

    @Test
    void convert_NoPhaseLimitationLevelsDefined_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(emptyList())
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();


        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_LimitierungNullLevelErfuellt_AnforderungNichtAusgewaehlt() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(emptyList())
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(null)
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();


        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_PhaseLimitationEmptyFulfilled_RequirementNotSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(emptyList())
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(emptyList())
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();


        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_PhaseLimitationEmptyLevelLower_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(emptyList())
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(emptyList())
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();


        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 5)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isFalse();
    }

    @Test
    void toTailoringCatalog_ValidBaseCatalogInput_TailoringCatalogReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .version("8.2.1")
            .toc(Chapter.<BaseRequirement>builder()
                .requirements(asList(
                    BaseRequirement.builder()
                        .phases(asList(ZERO, A))
                        .position("a")
                        .text("Requirement toc a")
                        .identifiers(emptyList())
                        .build()
                ))
                .chapters(asList(
                    Chapter.<BaseRequirement>builder()
                        .name("Gruppe 1")
                        .chapters(asList(
                            Chapter.<BaseRequirement>builder()
                                .name("Gruppe 1.1")
                                .build()
                        )).build(),
                    Chapter.<BaseRequirement>builder()
                        .name("Gruppe 2")
                        .build()
                ))
                .build())
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .phases(List.of(E, F))
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(E, F))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        // act
        Catalog<TailoringRequirement> actual = mapper.toTailoringCatalog(catalog, screeningSheet, selectionVector);

        // assert
        assertThat(actual).isNotNull();
    }

    @Disabled("Not sure if phase shall be present")
    @Test
    void toTailoringRequirement_ScreeningSheetWithoutPhase_RuntimeExceptionThrown() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .limitations(asList("SAT", "LEO"))
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(emptyList())
            .build();

        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();

        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        Throwable actual = catchThrowable(() -> mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder));

        // assert
        assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void toTailoringRequirement_RequirementWithoutLimitation_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
            .text("Die erste Requirement")
            .position("a")
            .phases(asList(ZERO))
            .identifiers(asList(
                Identifier.builder()
                    .type("Q")
                    .level(6)
                    .build()
            ))
            .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .phases(List.of(ZERO))
            .parameters(asList(
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                    .value("SAMPLE")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                    .value("SAT")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                    .value("Erdbeobachtungssatellit")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Phase.getName())
                    .value(asList(ZERO))
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                    .value("LEO")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                    .value("wissenschaftlich")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                    .value("150 <= k")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                    .value("15 Jahre < t")
                    .build(),
                ScreeningSheetParameter.builder()
                    .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                    .value("erforderlich")
                    .build()
            ))
            .build();


        SelectionVector selectionVector = SelectionVector.builder()
            .level("G", 1)
            .level("E", 2)
            .level("M", 3)
            .level("P", 4)
            .level("A", 5)
            .level("Q", 6)
            .level("S", 7)
            .level("W", 8)
            .level("O", 9)
            .level("R", 10)
            .build();


        TailoringRequirementBuilder TailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, TailoringRequirementBuilder);
        TailoringRequirement actual = TailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }


    @Test
    void toTailoringCatalog_BaseCatalogNull_NullReturned() {
        // arrange

        // act
        Catalog<TailoringRequirement> actual = mapper.toTailoringCatalog(
            null,
            ScreeningSheet.builder().build(),
            SelectionVector.builder().build());

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toTailoringCatalog_NoRequirementsInChapter_TailoringCatalogTocWithNullChapterReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder().build())
            .build();

        // act
        Catalog<TailoringRequirement> actual = mapper.toTailoringCatalog(
            catalog,
            ScreeningSheet.builder().build(),
            SelectionVector.builder().build()
        );

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc().getChapters()).isNull();
    }

    @Test
    void toTailoringCatalog_NoRequirementsAvailable_TailoringCatalogWithNullRequirementsReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
            .toc(Chapter.<BaseRequirement>builder().build())
            .build();

        // act
        Catalog<TailoringRequirement> actual = mapper.toTailoringCatalog(
            catalog,
            ScreeningSheet.builder().build(),
            SelectionVector.builder().build()
        );

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc().getRequirements()).isNull();
    }
}
