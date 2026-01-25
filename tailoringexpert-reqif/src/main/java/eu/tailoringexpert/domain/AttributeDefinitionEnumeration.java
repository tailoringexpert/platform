package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
public class AttributeDefinitionEnumeration extends AttributeDefinition {
    boolean multiValued;
    AttributeValueEnumeration defaultValue;
    DatatypeDefinitionEnumeration type;
}
