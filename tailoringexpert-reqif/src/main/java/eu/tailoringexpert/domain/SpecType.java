package eu.tailoringexpert.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter
@SuperBuilder
public abstract class SpecType extends Identifiable {
    Collection<AttributeDefinition> specAttributes;
}
