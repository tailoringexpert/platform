package eu.tailoringexpert.domain;

import lombok.Builder;
import lombok.Value;
import tools.jackson.dataformat.xml.annotation.JacksonXmlProperty;

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
