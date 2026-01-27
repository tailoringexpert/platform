package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
public class AttributeValueBoolean extends AttributeValueSimple {
    Boolean theValue;
    AttributeDefinitionBoolean definition;

}
