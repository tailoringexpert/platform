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

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

@Log4j2
class TailoringServiceMapperTest {

    TailoringServiceMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new TailoringServiceMapperGenerated();
    }

    @Test
    void convert_nullPhaseLimitationNoMatrix_RequirementSelectedBasedOnIdentifiers() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
                .text("Die erste Requirement")
                .position("a")
                .identifiers(asList(
                        Identifier.builder()
                                .type("Q")
                                .level(6)
                                .limitations(asList("SAT", "LEO"))
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
                .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtung")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .value("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_EmptyPhaseLimitationsNoMatrix_RequirementSelected() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
                .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtung")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_FulfilledPhaseLimitationNoMatrix_RequirementSelected() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
                .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .phases(List.of(E, F))
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtungssatellit")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_FulfilledPhaseLimitationWrongIdentifierNoMatrix_RequirementNotSelected() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
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
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isFalse();
    }

    @Test
    void convert_FulfilledPhaseLimitationAndIdentifierNoMatrix_RequirementSelected() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
                .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .phases(List.of(E, F))
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtungssatellit")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_erfuelltePhasenEinschraenkungIdenitifaktorenNoMatrix_AnforderungAusgewaehlt() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
                .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .phases(List.of(E, F))
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtungssatellit")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_PhaseLimitationNotFulfilledNoMatrix_RequirementNotSelected() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
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
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isFalse();
    }

    @Test
    void convert_NoPhaseLimitationLevelsDefinedNoMatrix_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
                .text("Die erste Requirement")
                .position("a")
                .phases(emptyList())
                .identifiers(asList(
                        Identifier.builder()
                                .type("Q")
                                .level(6)
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
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
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_LimitierungNullLevelErfuelltNoMatrix_AnforderungNichtAusgewaehlt() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
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
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_PhaseLimitationEmptyFulfilledNoMatrix_RequirementNotSelected() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
                .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtungssatellit")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void convert_PhaseLimitationEmptyLevelLowerNoMatrix_RequirementSelected() {
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
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1.1")
                .requirements(List.of(
                        requirement))
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
                                .build()))
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

        TailoringRequirementBuilder tailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isFalse();
    }

    @Test
    void toTailoringCatalog_ValidBaseCatalogInputNoMatrix_TailoringCatalogReturned() {
        // arrange

        Chapter<BaseRequirement> chapter1 = Chapter.<BaseRequirement>builder().name("Gruppe 1").number("1")
                .chapters(asList(
                        Chapter.<BaseRequirement>builder()
                                .name("Gruppe 1.1")
                                .number("1.1")
                                .requirements(List.of())
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter2 = Chapter.<BaseRequirement>builder().name("Gruppe 2").number("2")
                .requirements(List.of())
                .build();

        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
                .version("8.2.1")
                .toc(Chapter.<BaseRequirement>builder()
                        .requirements(asList(
                                BaseRequirement.builder()
                                        .phases(asList(ZERO, A))
                                        .position("a")
                                        .text("Requirement toc a")
                                        .identifiers(emptyList())
                                        .build()))
                        .chapters(asList(
                                chapter1,
                                chapter2))
                        .build())
                .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .phases(List.of(E, F))
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtungssatellit")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        Catalog<TailoringRequirement> actual = mapper.toTailoringCatalog(catalog, screeningSheet, selectionVector,
                empty());

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void toTailoringRequirement_RequirementWithoutLimitationNoMatrix_RequirementSelected() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
                .text("Die erste Requirement")
                .position("a")
                .phases(asList(ZERO))
                .identifiers(asList(
                        Identifier.builder()
                                .type("Q")
                                .level(6)
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1")
                .requirements(List.of(
                        requirement))
                .build();

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .phases(List.of(ZERO))
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtungssatellit")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, empty(),
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void toTailoringRequirement_RequirementInMatrix_MatrixState() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
                .text("Die erste Requirement")
                .position("a")
                .phases(asList(ZERO))
                .identifiers(asList(
                        Identifier.builder()
                                .type("Q")
                                .level(6)
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1")
                .requirements(List.of(
                        requirement))
                .build();

        Optional<Map<String, Collection<ImportRequirement>>> matrix = Optional.of(Map.ofEntries(
                entry("1", List.of(
                        ImportRequirement.builder().position("a").applicable("FALSE").build()))));

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .phases(List.of(ZERO))
                .parameters(List.of())
                .build();

        SelectionVector selectionVector = SelectionVector.builder().build();

        TailoringRequirementBuilder tailoringRequirementBuilder = TailoringRequirement.builder();

        // act
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, matrix,
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isFalse();
    }

    @Test
    void toTailoringRequirement_RequirementNotInMatrix_SelectionVectorStateCalculated() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
                .text("Die erste Requirement")
                .position("b")
                .phases(asList(ZERO))
                .identifiers(asList(
                        Identifier.builder()
                                .type("Q")
                                .level(6)
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1")
                .requirements(List.of(
                        requirement))
                .build();

        Optional<Map<String, Collection<ImportRequirement>>> matrix = Optional.of(Map.ofEntries(
                entry("1", List.of(
                        ImportRequirement.builder().position("a").applicable("FALSE").build()))));

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .phases(List.of(ZERO))
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtungssatellit")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, matrix,
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void toTailoringRequirement_ChapterNotInMatrix_SelectionVectorStateCalculated() {
        // arrange
        BaseRequirement requirement = BaseRequirement.builder()
                .text("Die erste Requirement")
                .position("b")
                .phases(asList(ZERO))
                .identifiers(asList(
                        Identifier.builder()
                                .type("Q")
                                .level(6)
                                .build()))
                .build();

        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1")
                .requirements(List.of(
                        requirement))
                .build();

        Optional<Map<String, Collection<ImportRequirement>>> matrix = Optional.of(Map.ofEntries(
                entry("2", List.of(
                        ImportRequirement.builder().position("a").applicable("FALSE").build()))));

        ScreeningSheet screeningSheet = ScreeningSheet.builder()
                .phases(List.of(ZERO))
                .parameters(asList(
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Identifier.getName())
                                .name("SAMPLE")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Produkttyp.getName())
                                .name("SAT")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzzweck.getName())
                                .name("Erdbeobachtungssatellit")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Einsatzort.getName())
                                .name("LEO")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Anwendungscharakter.getName())
                                .name("wissenschaftlich")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Kostenorientierug.getName())
                                .name("150 <= k")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.Lebensdauer.getName())
                                .name("15 Jahre < t")
                                .build(),
                        ScreeningSheetParameter.builder()
                                .category(ScreeningSheetDataProviderSupplier.ProgrammatischeBewertung.getName())
                                .name("erforderlich")
                                .build()))
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
        mapper.toTailoringRequirement(requirement, screeningSheet, selectionVector, chapter, matrix,
                tailoringRequirementBuilder);
        TailoringRequirement actual = tailoringRequirementBuilder.build();

        // assert
        assertThat(actual.getSelected()).isTrue();
    }

    @Test
    void toTailoringCatalog_BaseCatalogNullNoMatrix_NullReturned() {
        // arrange

        // act
        Catalog<TailoringRequirement> actual = mapper.toTailoringCatalog(
                null,
                ScreeningSheet.builder().build(),
                SelectionVector.builder().build(),
                empty());

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void toTailoringCatalog_NoRequirementsInChapter_TailoringCatalogTocWithEmptyChaptersReturned() {
        // arrange
        Chapter<BaseRequirement> chapter = Chapter.<BaseRequirement>builder().number("1")
                .requirements(List.of())
                .build();

        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
                .toc(chapter)
                .build();

        // act
        Catalog<TailoringRequirement> actual = mapper.toTailoringCatalog(
                catalog,
                ScreeningSheet.builder().build(),
                SelectionVector.builder().build(),
                empty());

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc().getChapters()).isEmpty();
    }

    @Test
    void toTailoringCatalog_NoRequirementsAvailableNoMatrix_TailoringCatalogWithEmptyRequirementListReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder()
                .toc(Chapter.<BaseRequirement>builder().build())
                .build();

        // act
        Catalog<TailoringRequirement> actual = mapper.toTailoringCatalog(
                catalog,
                ScreeningSheet.builder().build(),
                SelectionVector.builder().build(),
                empty());

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getToc().getRequirements()).isEmpty();
    }
}
