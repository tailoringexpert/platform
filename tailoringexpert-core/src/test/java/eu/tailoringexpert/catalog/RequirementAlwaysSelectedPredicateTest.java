package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Requirement;
import eu.tailoringexpert.domain.TailoringRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequirementAlwaysSelectedPredicateTest {
    RequirementAlwaysSelectedPredicate predicate;

    @BeforeEach
    void setup() {
        this.predicate = new RequirementAlwaysSelectedPredicate();
    }

    @Test
    void test_requirementSelected_trueReturned() {
        // arrange
        Requirement requirement = BaseRequirement.builder().build();

        // act
        boolean actual = predicate.test(requirement);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_requiremenTSelected_falseReturned() {
        // arrange
        Requirement requirement = TailoringRequirement.builder().selected(false).build();

        // act
        boolean actual = predicate.test(requirement);

        // assert
        assertThat(actual).isTrue();
    }
}
