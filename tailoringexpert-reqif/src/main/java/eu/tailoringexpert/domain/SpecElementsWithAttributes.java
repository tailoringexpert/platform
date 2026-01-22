package eu.tailoringexpert.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter
@SuperBuilder
public abstract class SpecElementsWithAttributes extends Identifiable {
    Collection<AttributeValue> values;
}
