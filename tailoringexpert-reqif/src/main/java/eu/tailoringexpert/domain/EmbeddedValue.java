package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class EmbeddedValue extends Identifiable{

    Integer key;
    String otherContent;
}
