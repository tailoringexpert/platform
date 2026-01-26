package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Value
@SuperBuilder
public class AttributeValueEnumeration extends AttributeValue {
    Collection<EnumValue> values;
    AttributeDefinitionEnumeration definition;
}
