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

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import com.tngtech.archunit.library.dependencies.SliceRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_JARS;
import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.GeneralCodingRules.ACCESS_STANDARD_STREAMS;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.library.plantuml.PlantUmlArchCondition.Configurations.consideringOnlyDependenciesInDiagram;
import static com.tngtech.archunit.library.plantuml.PlantUmlArchCondition.adhereToPlantUmlDiagram;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureTest {

    static String PACKAGEIDENTIFIERS = "eu.tailoringexpert..";
    /**
     * Liste der zu prüfenden Spring  Annotationen.
     */
    static final List<String> SPRING_ANNOTATIONS = asList(
        "org.springframework.context.annotation.Bean",
        "org.springframework.context.annotation.ComponentScan",
        "org.springframework.context.annotation.ComponentScans",
        "org.springframework.context.annotation.Conitional",
        "org.springframework.context.annotation.Configuration",
        "org.springframework.context.annotation.DependsOn",
        "org.springframework.context.annotation.Description",
        "org.springframework.context.annotation.EnableAspectJAutoProxy",
        "org.springframework.context.annotation.EnableLoadTimeWeaving",
        "org.springframework.context.annotation.EnableMBeanExport",
        "org.springframework.context.annotation.Import",
        "org.springframework.context.annotation.ImportResource",
        "org.springframework.context.annotation.Lazy",
        "org.springframework.context.annotation.Primary",
        "org.springframework.context.annotation.Profile",
        "org.springframework.context.annotation.PropertySource",
        "org.springframework.context.annotation.PropertySources",
        "org.springframework.context.annotation.Role",
        "org.springframework.context.annotation.Scope",
        "org.springframework.stereotype.Service",
        "org.springframework.stereotype.Component",
        "org.springframework.stereotype.Controller",
        "org.springframework.stereotype.Repository",
        "org.springframework.beans.factory.annotation.Autowired",
        "org.springframework.beans.factory.annotation.Configurable",
        "org.springframework.beans.factory.annotation.Lookup",
        "org.springframework.beans.factory.annotation.Qualifier",
        "org.springframework.beans.factory.annotation.Required",
        "org.springframework.beans.factory.annotation.Value");

    /**
     * Liste der zu prüfenden Swagger Annotationen.
     */
    static final List<String> SWAGGER_ANNOTATIONS = asList(
        "io.swagger.annotations.Api",
        "io.swagger.annotations.ApiImplicitParam",
        "io.swagger.annotations.ApiImplicitParams",
        "io.swagger.annotations.ApiModel",
        "io.swagger.annotations.ApiModelProperty",
        "io.swagger.annotations.ApiOperation",
        "io.swagger.annotations.ApiParam",
        "io.swagger.annotations.ApiResponse",
        "io.swagger.annotations.ApiResponses",
        "io.swagger.annotations.Authorization",
        "io.swagger.annotations.AuthorizationScope",
        "io.swagger.annotations.Contact",
        "io.swagger.annotations.ExampleProperty",
        "io.swagger.annotations.Extension",
        "io.swagger.annotations.ExternalDocs",
        "io.swagger.annotations.Info",
        "io.swagger.annotations.License",
        "io.swagger.annotations.ResponseHeader",
        "io.swagger.annotations.SwaggerDefinition",
        "io.swagger.annotations.Tag");

    /**
     * Liste der zu prüfenden Jackson/Json Annotationen.
     */
    static final List<String> JACKSON_ANNOTATIONS = asList(
        "com.fasterxml.jackson.annotation.JacksonAnnotation",
        "com.fasterxml.jackson.annotation.JacksonAnnotationsInside",
        "com.fasterxml.jackson.annotation.JacksonAnnotationValue",
        "com.fasterxml.jackson.annotation.JacksonInject",
        "com.fasterxml.jackson.annotation.JsonAnyGetter",
        "com.fasterxml.jackson.annotation.AnySetter",
        "com.fasterxml.jackson.annotation.AutoDetect",
        "com.fasterxml.jackson.annotation.JsonBackReference",
        "com.fasterxml.jackson.annotation.JsonClassDescription",
        "com.fasterxml.jackson.annotation.JsonCreator",
        "com.fasterxml.jackson.annotation.JsonEnumDefaultValue",
        "com.fasterxml.jackson.annotation.JsonFilter",
        "com.fasterxml.jackson.annotation.JsonFormat",
        "com.fasterxml.jackson.annotation.JsonGetter",
        "com.fasterxml.jackson.annotation.JsonIdentityInfo",
        "com.fasterxml.jackson.annotation.JsonIgnore",
        "com.fasterxml.jackson.annotation.JsonIgnoreProperties",
        "com.fasterxml.jackson.annotation.JsonIgnoreType",
        "com.fasterxml.jackson.annotation.JsonInclude",
        "com.fasterxml.jackson.annotation.JsonManagedReference",
        "com.fasterxml.jackson.annotation.JsonProperty",
        "com.fasterxml.jackson.annotation.JsonPropertyDescription",
        "com.fasterxml.jackson.annotation.JsonPropertyOrder",
        "com.fasterxml.jackson.annotation.JsonRawValue",
        "com.fasterxml.jackson.annotation.JsonRootName",
        "com.fasterxml.jackson.annotation.JsonSetter",
        "com.fasterxml.jackson.annotation.JsonSubTypes",
        "com.fasterxml.jackson.annotation.JsonTypeId",
        "com.fasterxml.jackson.annotation.JsonTypeInfo",
        "com.fasterxml.jackson.annotation.JsonTypeName",
        "com.fasterxml.jackson.annotation.JsonUnwrapped",
        "com.fasterxml.jackson.annotation.JsonValue",
        "com.fasterxml.jackson.annotation.JsonView"
    );

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
            .withImportOption(DO_NOT_INCLUDE_JARS)
            .withImportOption(DO_NOT_INCLUDE_TESTS)
            .importClasspath();
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
    void cycles() {
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
//    @Test
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


    @DisplayName("Keine Abhängigkeiten zu Spring Annotationen")
    @Test
    void noSpringDependency() {
        // arrange
        ArchRule rule = noClasses()
            .that(isAnnotatedWith(SPRING_ANNOTATIONS))
            .or(haveAFieldAnnotatedWith(SPRING_ANNOTATIONS))
            .should()
            .resideInAnyPackage("eu.tailoringexpert..")
            .allowEmptyShould(true);


        // act
        EvaluationResult actual = rule.evaluate(classes);

        // assert
        assertThat(actual.hasViolation()).isFalse();
    }

    @DisplayName("Keine Abhängigkeiten zu Spring Annotationen")
    @Test
    void springUsage() {
        // arrange
        ClassesShouldConjunction rule = noClasses().that()
            .resideInAnyPackage("eu.tailoringexpert..")
            .should().accessClassesThat().resideInAnyPackage("org.springframework..");

        // act
        EvaluationResult actual = rule.evaluate(classes);

        // assert
        assertThat(actual.hasViolation()).isFalse();
    }


    @DisplayName("Keine Verwendung von System.out und System.err")
    @Test
    void classeShouldNotAccessStandardStreamsDefinedByHand() {
        // arrange
        ClassesShouldConjunction rule = noClasses().should(ACCESS_STANDARD_STREAMS);

        // act
        EvaluationResult actual = rule.evaluate(classes);

        // assert
        assertThat(actual.hasViolation()).isFalse();
    }


    /**
     * Prüft, ob eine Klasse mindestens ein Attribut mit einer der übergebenen Annotationen besitzt.
     *
     * @param annotations Mögliche Annotationen
     * @return true, wenn die Klasse mindestens ein Attribut mit einer der übergebenen Annotationen besitzt
     */
    static DescribedPredicate<JavaClass> haveAFieldAnnotatedWith(final List<String> annotations) {
        return new DescribedPredicate<JavaClass>("class has a least one field annotated with given annotation") {
            @Override
            public boolean apply(final JavaClass input) {
                return input.getFields()
                    .stream()
                    .filter(f -> f.getAnnotations()
                        .stream()
                        .map(t -> t.getRawType().getName())
                        .filter(t -> annotations.contains(t))
                        .findFirst()
                        .isPresent()
                    )
                    .findFirst()
                    .isPresent();
            }
        };
    }


    /**
     * Prüft, ob eine Klasse einer der übergebenen Annotationen besitzt.
     *
     * @param annotations Mögliche Annotationen
     * @return true, wenn die Klasse mindestens eine der übergebenen Annotationen besitzt
     */
    static DescribedPredicate<JavaClass> isAnnotatedWith(final List<String> annotations) {
        return new DescribedPredicate<JavaClass>("class has at least one of given annotation") {
            @Override
            public boolean apply(final JavaClass input) {
                return input.getAnnotations()
                    .stream()
                    .filter(c -> annotations.contains(c.getRawType().getName()))
                    .findFirst()
                    .isPresent();
            }
        };
    }

}
