package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class ToChapterHeaderValueTest {

    ToChapterHeaderValue toChaperHeader;

    @BeforeEach
    void setup() {
        this.toChaperHeader = new ToChapterHeaderValue();
    }

    @Test
    void apply_ValidValue_AttrNameReturned() {
        // arrange

        // act
        AttributeValueString actual = toChaperHeader.apply("1. General");

        // assert
        AttributeValueString expected = AttributeValueString.builder()
            .theValue("1. General")
            .definition(AttributeDefinitionString.builder()
                .type(DatatypeDefinitionString.builder()
                    .maxLength(200)
                    .identifier("attr_name")
                    .longName("Name of the chapter")
                    .build())
                .build())
            .build();

        assertThat(actual)
            .isEqualTo(expected);
    }
}
