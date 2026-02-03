package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
public class AttributeDefinitionXHTML extends AttributeDefinition {
    DatatypeDefinitionXHTML type;
    XhtmlContent theValue;
    XhtmlContent theOriginalValue;
}
