package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.Collection;

@Value
@SuperBuilder
public class SpecHierarchy extends AccessControlledElement{

    boolean tableInternal = false;
    Collection<SpecHierarchy> children;
    Collection<AttributeDefinition> editableAtts;
    SpecObject object;
}
