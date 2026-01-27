package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
public class AttributeValueString extends AttributeValueSimple {
    String theValue;

}
