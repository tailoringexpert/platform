package eu.tailoringexpert.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.time.LocalDateTime;


@Getter
@SuperBuilder
public abstract class Identifiable {
    @JacksonXmlProperty(isAttribute = true)
    String desc;

    @JacksonXmlProperty(isAttribute = true)
    String identifier;

    @JacksonXmlProperty(isAttribute = true)
    LocalDateTime lastChange;

    @JacksonXmlProperty(isAttribute = true)
    String longName;

}
