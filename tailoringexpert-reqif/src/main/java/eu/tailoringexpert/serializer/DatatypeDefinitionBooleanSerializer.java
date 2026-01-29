package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.reqif.IdentifiableConsumer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class DatatypeDefinitionBooleanSerializer extends StdSerializer<DatatypeDefinitionBoolean> {

    private static final QName QNAME_DATATYPEDEFINITIONBOOLEAN = new QName("DATATYPE-DEFINITION-BOOLEAN");

    BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public DatatypeDefinitionBooleanSerializer() {
        super(DatatypeDefinitionBoolean.class);
    }

    @Override
    public void serialize(DatatypeDefinitionBoolean value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_DATATYPEDEFINITIONBOOLEAN);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);
        generator.writeEndObject();
    }
}
