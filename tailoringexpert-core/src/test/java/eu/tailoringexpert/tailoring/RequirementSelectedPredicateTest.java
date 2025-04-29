package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.TailoringRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequirementSelectedPredicateTest {

    RequirementSelectedPredicate predicate;

    @BeforeEach
    void setup() {
        this.predicate = new RequirementSelectedPredicate();
    }

    @Test
    void test_requirementSelected_trueReturned() {
        // arrange
        TailoringRequirement requirement = TailoringRequirement.builder().selected(true).build();

        // act
        boolean actual = predicate.test(requirement);

        // assert
        assertThat(actual).isTrue();
    }

    @Test
    void test_requirementNotSelected_falseReturned() {
        // arrange
        TailoringRequirement requirement = TailoringRequirement.builder().selected(false).build();

        // act
        boolean actual = predicate.test(requirement);

        // assert
        assertThat(actual).isFalse();
    }
}
