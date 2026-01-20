package eu.tailoringexpert.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.time.LocalDateTime;
import java.util.Collection;

@Value
@Builder
public class ReqIF {

    @JacksonXmlProperty(localName = "REQ-IF-HEADER")
    ReqIFHeader theHeader;

    @JacksonXmlProperty(localName = "REQ-IF-CONTENT")
    ReqIFContent coreContent;


    Collection<ReqIFToolExtension> toolExtensions;


}
