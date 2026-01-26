package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinition;
import eu.tailoringexpert.domain.ReqIFContent;
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


public class ReqIFContentSerializer extends StdSerializer<ReqIFContent> {

    private BiConsumer<Collection<DatatypeDefinition>, ToXmlGenerator> datatypes = new DatatypeDefinitionsConsumer();
    private BiConsumer<Collection<SpecType>, ToXmlGenerator> specTypes = new SpecTypesConsumer();
    private BiConsumer<Collection<SpecObject>, ToXmlGenerator> specObjects = new SpecObjectsConsumer();

    public ReqIFContentSerializer() {
        super(ReqIFContent.class);
    }

    @Override
    public void serialize(ReqIFContent value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        QName parent = new QName("CORE-CONTENT");
        QName name = new QName("REQ-IF-CONTENT");

        ToXmlGenerator generator = (ToXmlGenerator) gen;
        generator.startWrappedValue(parent, name);
        generator.writeStartObject();

        datatypes.accept(value.getDatatypes(), generator);
        specTypes.accept(value.getSpecTypes(), generator);
        specObjects.accept(value.getSpecObjects(), generator);

        generator.writeEndObject();
        generator.finishWrappedValue(parent, name);
    }


}

