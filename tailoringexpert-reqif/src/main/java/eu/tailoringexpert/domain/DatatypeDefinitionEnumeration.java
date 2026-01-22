package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Value
@SuperBuilder
public class DatatypeDefinitionEnumeration extends DatatypeDefinition {
    Collection<EnumValue> specifiedValues;
}
