package eu.tailoringexpert.domain;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import tools.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.LocalDateTime;

@Value
@SuperBuilder
//@JsonRootName("DATATYPE-DEFINITION-STRING")
public class DatatypeDefinitionString extends DatatypeDefinition  {

    @JacksonXmlProperty(isAttribute = false)
    int maxLength;

}
