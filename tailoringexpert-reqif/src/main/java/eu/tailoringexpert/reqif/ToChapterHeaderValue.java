package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionString;

import java.util.function.Function;

public class ToChapterHeaderValue implements Function<String, AttributeValueString> {

    AttributeDefinitionString definition = AttributeDefinitionString.builder()
        .identifier("attr_name")
        .longName("Name of the chapter")
        .type(DatatypeDefinitionString.builder()
            .maxLength(200)
            .identifier("dt-string")
            .longName("String")
            .build())
        .build();

    @Override
    public AttributeValueString apply(String value) {
        return AttributeValueString.builder()
            .definition(definition)
            .theValue(value)
            .build();
    }
}
