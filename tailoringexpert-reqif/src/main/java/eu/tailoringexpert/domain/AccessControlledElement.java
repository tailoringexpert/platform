package eu.tailoringexpert.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class AccessControlledElement extends Identifiable{

    boolean editable;
}
