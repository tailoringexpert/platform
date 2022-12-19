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
package eu.tailoringexpert;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import com.tngtech.archunit.library.dependencies.SliceRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_JARS;
import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.Configuration.consideringOnlyDependenciesInDiagram;
import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.adhereToPlantUmlDiagram;
import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureTest {

    static JavaClasses classes;

    static String PACKAGEIDENTIFIERS = "eu.tailoringexpert..";

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_TESTS)
            .withImportOption(DO_NOT_INCLUDE_JARS)
            .importPaths("target");
    }

    @DisplayName("Keine Deprecated Verwendung")
    @Test
    void noDeprecated() {
        // arrange
        ArchRule rule = noClasses()
            .that().resideInAnyPackage(PACKAGEIDENTIFIERS)
            .should().beAnnotatedWith(Deprecated.class)
            .because("deprecated classes should not be allowed in package eu.tailoringexpert");

        // act
        EvaluationResult actual = rule.evaluate(classes);

        // assert
        assertThat(actual.hasViolation()).isFalse();
    }

    @DisplayName("Zyklenfreiheit")
    @Test
    void noCycles() {
        // arrange
        SliceRule rule = slices().matching("eu.(tailoringexpert).(*)")
            .namingSlices("$2 of $1").should()
            .beFreeOfCycles();

        // act
        EvaluationResult actual = rule.evaluate(classes);

        // assert
        assertThat(actual.hasViolation()).isFalse();
    }

    @DisplayName("Layerzugriffe")
    @Test
    void layerDependenciesAreRespected() {
        // arrange
        ClassesShouldConjunction rule = classes()
            .should(adhereToPlantUmlDiagram(
                getClass().getResource("/archunit.plantuml"),
                consideringOnlyDependenciesInDiagram()));
        // act
        EvaluationResult actual = rule.evaluate(classes);

        // assert
        assertThat(actual.hasViolation()).isFalse();
    }

    @DisplayName("No unwanted package dependencies")
    @ParameterizedTest(name = "{index} is free of unwanted references to {0}")
    @ValueSource(strings = {"org.springframework.", "io.swagger.", "com.fasterxml."})
    void noDependencies(String pkg) {
        // arrange
        ArchRule rule = noClasses()
            .that().resideInAnyPackage(PACKAGEIDENTIFIERS)
            .should().dependOnClassesThat().haveNameMatching(pkg)
            .because("core should be free from " + pkg);

        // act
        EvaluationResult actual = rule.evaluate(classes);

        // assert
        assertThat(actual.hasViolation()).isFalse();
    }


    @DisplayName("Keine Verwendung von System.out und System.err")
    @Test
    void classesShouldNotAccessStandardStreamsDefinedByHand() {
        // arrange
        ArchRule rule = noClasses()
            .that().resideInAnyPackage(PACKAGEIDENTIFIERS)
            .should(ACCESS_STANDARD_STREAMS)
            .because("system streams are not allowed");

        // act
        EvaluationResult actual = rule.evaluate(classes);

        // assert
        assertThat(actual.hasViolation()).isFalse();
    }
}
