package eu.tailoringexpert.domain;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class AttributeDefinition extends Identifiable {

    abstract public DatatypeDefinition getType();
}
