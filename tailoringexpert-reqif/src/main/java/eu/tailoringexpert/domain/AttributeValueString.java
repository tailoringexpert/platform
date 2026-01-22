package eu.tailoringexpert.domain;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@SuperBuilder
@Value
public class AttributeValueString extends AttributeValueSimple {
    String theValue;

}
