package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class AttributeDefinitionString extends AttributeDefinitionSimple {
    DatatypeDefinitionString type;
}
