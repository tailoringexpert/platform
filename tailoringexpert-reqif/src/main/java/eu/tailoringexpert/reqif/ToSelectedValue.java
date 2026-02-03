package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.TailoringRequirement;

import java.util.function.Function;

public class ToSelectedValue implements Function<TailoringRequirement, AttributeValueBoolean> {

    private AttributeDefinitionBoolean definition =
        AttributeDefinitionBoolean.builder()
            .type(DatatypeDefinitionBoolean.builder()
                .identifier("a-active")
                .longName("Active")
                .build())
            .build();

    @Override
    public AttributeValueBoolean apply(TailoringRequirement value) {
        return AttributeValueBoolean.builder()
            .definition(definition)
            .theValue(value.getSelected())
            .build();
    }
}
