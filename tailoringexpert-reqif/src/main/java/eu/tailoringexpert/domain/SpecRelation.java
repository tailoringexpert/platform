package eu.tailoringexpert.domain;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
public class SpecRelation extends SpecElementsWithAttributes{

    private SpecObject source;
    private SpecObject target;

    SpecRelationType type;
}
