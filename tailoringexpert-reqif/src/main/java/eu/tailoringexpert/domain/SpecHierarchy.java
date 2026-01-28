package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class SpecHierarchy extends AccessControlledElement{

    boolean tableInternal = false;
}
