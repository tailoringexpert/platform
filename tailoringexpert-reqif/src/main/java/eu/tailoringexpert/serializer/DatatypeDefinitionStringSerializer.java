package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class DatatypeDefinitionStringSerializer extends StdSerializer<DatatypeDefinitionString> {

    private static final QName QNAME_DATATYPEDEFINITION = new QName("DATATYPE-DEFINITION-STRING");
    private static final String PROPERTY_MAXLENGTH = "MAX-LENGTH";

    BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public DatatypeDefinitionStringSerializer() {
        super(DatatypeDefinitionString.class);
    }

    @Override
    public void serialize(DatatypeDefinitionString value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_DATATYPEDEFINITION);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);

        generator.setNextIsAttribute(true);
        generator.writeNumberProperty(PROPERTY_MAXLENGTH, value.getMaxLength());

        generator.writeEndObject();
    }
}
