package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
public class AttributeValueXHTML extends AttributeValue {

    AttributeDefinitionXHTML definition;
    boolean simplified;


}
