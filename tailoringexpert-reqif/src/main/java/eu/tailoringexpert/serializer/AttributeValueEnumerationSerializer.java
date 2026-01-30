package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class AttributeValueEnumerationSerializer extends StdSerializer<AttributeValueEnumeration> {

    private static final QName QNAME_DEFINITION = new QName("DEFINITION");
    private static final QName QNAME_ATTRIBUTEVALUE = new QName("ATTRIBUTE-VALUE-ENUMERATION");
    private static final QName QNAME_VALUES = new QName("VALUES");
    private static final String PROPERTY_ATTRIBUTEDEFINITIONREF = "ATTRIBUTE-DEFINITION-ENUMERATION-REF";
    private static final String PROPERTY_ENUMREF = "ENUM-VALUE-REF";

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public AttributeValueEnumerationSerializer() {
        super(AttributeValueEnumeration.class);
    }

    @Override
    public void serialize(AttributeValueEnumeration value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_ATTRIBUTEVALUE);
        generator.writeStartObject();
        identifiable.accept(value, generator);

        generator.startWrappedValue(QNAME_VALUES, QNAME_VALUES);
        value.getValues()
            .forEach(ref -> generator.writeStringProperty(PROPERTY_ENUMREF, ref.getIdentifier()));
        generator.finishWrappedValue(QNAME_VALUES, QNAME_VALUES);

        generator.startWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);
        generator.writeStringProperty(PROPERTY_ATTRIBUTEDEFINITIONREF, value.getDefinition().getIdentifier());
        generator.finishWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);

        generator.writeEndObject();

    }

}
