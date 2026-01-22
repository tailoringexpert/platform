package eu.tailoringexpert.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

@Value
@SuperBuilder
public class AttributeDefinitionString extends AttributeDefinitionSimple {
    DatatypeDefinitionString type;
}
