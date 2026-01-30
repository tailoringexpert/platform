package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class AttributeDefinitionEnumerationSerializer extends StdSerializer<AttributeDefinitionEnumeration> {

    private static final QName QNAME_ATTRIBUTEDEFINITION = new QName("ATTRIBUTE-DEFINITION-ENUMERATION");
    private static final QName QNAME_TYPE = new QName("TYPE");
    private static final String PROPERTY_DATATYPEDEFINITIONREF = "DATATYPE-DEFINITION-ENUMERATION-REF";

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public AttributeDefinitionEnumerationSerializer() {
        super(AttributeDefinitionEnumeration.class);
    }

    @Override
    public void serialize(AttributeDefinitionEnumeration value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_ATTRIBUTEDEFINITION);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);

        generator.startWrappedValue(QNAME_TYPE, QNAME_TYPE);
        generator.writeStringProperty(PROPERTY_DATATYPEDEFINITIONREF, value.getType().getIdentifier());
        generator.finishWrappedValue(QNAME_TYPE, QNAME_TYPE);

        generator.writeEndObject();
    }
}
