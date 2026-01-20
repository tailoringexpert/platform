package eu.tailoringexpert.domain;

import lombok.Getter;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Getter
@SuperBuilder
public abstract class SpecElementsWithAtributes extends Identifiable {

    Collection<AttributeValue> values;
}
