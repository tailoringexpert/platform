package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinition;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.ReqIFContent;
import eu.tailoringexpert.domain.ReqIFHeader;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.function.BiConsumer;


public class ReqIFHeaderSerializer extends StdSerializer<ReqIFHeader> {

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public ReqIFHeaderSerializer() {
        super(ReqIFContent.class);
    }

    @Override
    public void serialize(ReqIFHeader value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        QName parent = new QName("THE-HEADER");
        QName name = new QName("REQ-IF-HEADER");

        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.startWrappedValue(parent, name);
//        generator.setNextIsAttribute(true);
//        generator.writeStringProperty("IDENTIFIER", value.getIdentifier());

        generator.setNextIsAttribute(false);
        generator.writeStartObject();
        generator.writeStringProperty("REQ-IF-TOOL-ID", value.getReqIFToolId());
        generator.writeStringProperty("REQ-IF-VERSION", value.getReqIFVersion());
        generator.writeStringProperty("SOURCE-TOOL-ID", value.getSourceToolId());
        generator.writeStringProperty("TITEL", value.getTitle());


        generator.writeEndObject();
        generator.finishWrappedValue(parent, name);
    }


}

