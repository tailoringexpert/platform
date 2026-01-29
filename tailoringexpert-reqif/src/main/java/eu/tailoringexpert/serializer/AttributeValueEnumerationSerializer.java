package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.reqif.IdentifiableConsumer;
import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class AttributeValueEnumerationSerializer extends StdSerializer<AttributeValueEnumeration> {

    private static final QName QNAME_DEFINITION = new QName("DEFINITION");
    private static final QName QNAME_ATTRIBUTEVALUEENUMERATION = new QName("ATTRIBUTE-VALUE-ENUMERATION");
    private static final QName QNAME_VALUES = new QName("VALUES");
    private static final String PROPERTY_ENUMVALUEREF = "ENUM-VALUE-REF";
    private static final String PROPERTY_ATTRIBUTEDEFINITIONENUMERATIONREF = "ATTRIBUTE-DEFINITION-ENUMERATION-REF";

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public AttributeValueEnumerationSerializer() {
        super(AttributeValueEnumeration.class);
    }

    @Override
    public void serialize(AttributeValueEnumeration value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_ATTRIBUTEVALUEENUMERATION);
        generator.writeStartObject();
        identifiable.accept(value, generator);

        generator.startWrappedValue(QNAME_VALUES, QNAME_VALUES);
        value.getValues()
            .forEach(ref -> generator.writeStringProperty(PROPERTY_ENUMVALUEREF, ref.getIdentifier()));
        generator.finishWrappedValue(QNAME_VALUES, QNAME_VALUES);

        generator.startWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);
        generator.writeStringProperty(PROPERTY_ATTRIBUTEDEFINITIONENUMERATIONREF, value.getDefinition().getIdentifier());
        generator.finishWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);

        generator.writeEndObject();

    }

}
