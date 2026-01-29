package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.reqif.IdentifiableConsumer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class AttributeDefinitionStringSerializer extends StdSerializer<AttributeDefinitionString> {

    private static final QName QNAME_ATTRIBUTEDEFINITIONSTRING  = new QName("ATTRIBUTE-DEFINITION-STRING");

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public AttributeDefinitionStringSerializer() {
        super(AttributeDefinitionString.class);
    }

    @Override
    public void serialize(AttributeDefinitionString value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_ATTRIBUTEDEFINITIONSTRING);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);

        generator.startWrappedValue(new QName("TYPE"), new QName("TYPE"));
        generator.writeStringProperty("DATATYPE-DEFINITION-STRING-REF", value.getType().getIdentifier());
        generator.finishWrappedValue(new QName("TYPE"), new QName("TYPE"));

        generator.writeEndObject();
    }
}
