package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.DatatypeDefinition;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.ReqIFContent;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;


public class ReqIFContentSerializer extends StdSerializer<ReqIFContent> {

    private BiConsumer<ToXmlGenerator, Identifiable> identifiable = new IdentifiableConsumer();
    private BiConsumer<ToXmlGenerator, Collection<SpecType>> specTypes = new SpecTypesConsumer();
    private BiConsumer<ToXmlGenerator, Collection<DatatypeDefinition>> datatypes = new DatatypeDefinitionsConsumer();


    public ReqIFContentSerializer() {
        super(ReqIFContent.class);
    }

    @Override
    public void serialize(ReqIFContent value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator xml = (ToXmlGenerator) gen;
        xml.setNextName(new QName("", "SPEC-OBJECT-TYPE"));
        //xml.startWrappedValue(new QName("", "REQ-IF-CONTENT"), new QName("", "REQ-IF-CONTENT"));
        xml.writeStartObject();

        datatypes.accept(xml, value.getDatatypes());
        specTypes.accept(xml, value.getSpecTypes());

        xml.writeEndObject();
    }





}

