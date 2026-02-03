package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.TailoringRequirement;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class ToRequirementValueTest {

    private static final String PREFIX = "reqif-xhtml:";

    ToRequirementValue toRequirement;

    @BeforeEach
    void setUp() {
        this.toRequirement = new ToRequirementValue();
    }

    @Test
    void apply_ValidValue_ADescriptionReturned() {
        // arrange

        // act
        AttributeValueString actual = toRequirement.apply(TailoringRequirement.builder()
            .text("This is a requirement text")
            .build());

        // assert
        AttributeValueString expected = AttributeValueString.builder()
            .theValue("This is a requirement text")
            .definition(AttributeDefinitionString.builder()
                .type(DatatypeDefinitionString.builder()
                    .maxLength(100000)
                    .identifier("a-description")
                    .longName("Description")
                    .build())
                .build())
            .build();

        assertThat(actual)
            .isEqualTo(expected);
    }
}
