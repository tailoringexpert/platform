package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class AttributeValueStringSerializer extends StdSerializer<AttributeValueString> {

    private static final QName QNAME_ATTRIBUTEVALUE = new QName("ATTRIBUTE-VALUE-STRING");
    private static final QName QNAME_DEFINITION = new QName("DEFINITION");
    private static final String PROPERTY_ATTRIBUTEDEFININITIONREF = "ATTRIBUTE-DEFINITION-STRING-REF";
    private static final String PROPERTY_THEVALUE = "THE-VALUE";

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public AttributeValueStringSerializer() {
        super(AttributeValueString.class);
    }

    @Override
    public void serialize(AttributeValueString value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_ATTRIBUTEVALUE);
        generator.writeStartObject();
        identifiable.accept(value, generator);

        generator.setNextIsAttribute(true);
        generator.writeStringProperty(PROPERTY_THEVALUE, value.getTheValue());

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);
        generator.writeStringProperty(PROPERTY_ATTRIBUTEDEFININITIONREF, value.getDefinition().getType().getIdentifier());
        generator.finishWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);

        generator.writeEndObject();
    }
}
