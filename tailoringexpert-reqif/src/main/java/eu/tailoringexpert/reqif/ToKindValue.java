package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.DatatypeDefinitionEnumeration;
import eu.tailoringexpert.domain.EmbeddedValue;
import eu.tailoringexpert.domain.EnumValue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public class ToKindValue implements Function<Integer, AttributeValueEnumeration> {

    AttributeDefinitionEnumeration definition = AttributeDefinitionEnumeration.builder()
        .identifier("a-type")
        .longName("Type")
        .type(DatatypeDefinitionEnumeration.builder()
            .identifier("dt-kind")
            .longName("Kind")
            .lastChange(LocalDateTime.now())
            .specifiedValues(List.of(
                    EnumValue.builder()
                        .identifier("v-req")
                        .longName("Requirement")
                        .properties(EmbeddedValue.builder()
                            .key(0)
                            .otherContent("Requirement")
                            .build())
                        .build(),
                    EnumValue.builder()
                        .identifier("v-moc")
                        .longName("Means of Compliance")
                        .properties(EmbeddedValue.builder()
                            .key(1)
                            .otherContent("Means of Compliance")
                            .build())
                        .build(),
                    EnumValue.builder()
                        .identifier("v-tp")
                        .longName("Technical Procedure")
                        .properties(EmbeddedValue.builder()
                            .key(2)
                            .otherContent("Technical Procedure")
                            .build())
                        .build(),
                    EnumValue.builder()
                        .identifier("v-ts")
                        .longName("Technical Specification")
                        .properties(EmbeddedValue.builder()
                            .key(3)
                            .otherContent("Technical Specification")
                            .build())
                        .build()
                )
            )
            .build())
        .build();

    @Override
    public AttributeValueEnumeration apply(Integer value) {
        return AttributeValueEnumeration.builder()
            .definition(definition)
            .values(List.of(
                EnumValue.builder()
                    .properties(
                        EmbeddedValue.builder()
                            .key(value)
                            .build()
                    )
                    .build())
            )
            .build();
    }
}
