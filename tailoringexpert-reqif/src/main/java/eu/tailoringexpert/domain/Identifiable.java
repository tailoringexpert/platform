package eu.tailoringexpert.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.time.LocalDateTime;


@Getter
@SuperBuilder
public abstract class Identifiable {
    @JacksonXmlProperty(isAttribute = false)
    String desc;
    @JacksonXmlProperty(isAttribute = false)
    String identifier;
    LocalDateTime lastChange;
    @JacksonXmlProperty(isAttribute = false)
    String longName;

}
