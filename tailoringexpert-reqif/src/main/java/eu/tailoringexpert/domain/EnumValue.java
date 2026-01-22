package eu.tailoringexpert.domain;

import lombok.Getter;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
public class EnumValue extends Identifiable {

    DatatypeDefinitionEnumeration dataTpeDefEnum;
}
