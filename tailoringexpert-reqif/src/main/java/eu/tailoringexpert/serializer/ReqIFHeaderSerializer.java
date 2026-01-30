package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.ReqIFHeader;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;

public class ReqIFHeaderSerializer extends StdSerializer<ReqIFHeader> {

    private static final QName QNAME_HEADER = new QName("REQ-IF-HEADER");
    private static final String PROPERTY_IDENTIFIER = "IDENTIFIER";
    private static final String PROPERTY_IFTOOLID = "REQ-IF-TOOL-ID";
    private static final String PROPERTY_VERSION = "REQ-IF-VERSION";
    private static final String PROPERTY_SOURCETOOLID = "SOURCE-TOOL-ID";
    private static final String PROPERTY_TITLE = "TITLE";

    public ReqIFHeaderSerializer() {
        super(ReqIFHeader.class);
    }

    @Override
    public void serialize(ReqIFHeader value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_HEADER);
        generator.writeStartObject();

        generator.setNextIsAttribute(true);
        generator.writeStringProperty(PROPERTY_IDENTIFIER, value.getIdentifier());

        generator.setNextIsAttribute(false);
        generator.writeStringProperty(PROPERTY_IFTOOLID, value.getReqIFToolId());
        generator.writeStringProperty(PROPERTY_VERSION, value.getReqIFVersion());
        generator.writeStringProperty(PROPERTY_SOURCETOOLID, value.getSourceToolId());
        generator.writeStringProperty(PROPERTY_TITLE, value.getTitle());

        generator.writeEndObject();
    }
}

