package eu.tailoringexpert.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class AttributeValue extends Identifiable{
    AttributeDefinition definition;
}
