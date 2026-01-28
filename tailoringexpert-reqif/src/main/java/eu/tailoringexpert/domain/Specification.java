package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Value
@SuperBuilder
public class Specification extends SpecElementsWithAttributes {
    SpecificationType type;
    Collection<SpecHierarchy> children;

}
