package eu.tailoringexpert.domain;

import lombok.experimental.SuperBuilder;

import java.util.Collection;

@SuperBuilder
public class Specification extends SpecElementsWithAttributes {
    SpecificationType type;
    Collection<SpecHierarchy> children;

}
