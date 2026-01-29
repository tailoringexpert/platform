package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValue;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.reqif.IdentifiableConsumer;
import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.Optional.ofNullable;

@Log4j2
public class AttributeValueStringSerializer extends StdSerializer<AttributeValueString> {

    private static final QName QNAME_ATTRIBUTEVALUESTRING = new QName("ATTRIBUTE-VALUE-STRING");
    private static final String PROPERTY_THEVALUE = "THE-VALUE";
    private static final String PROPERTY_ATTRIBUTEDEFININITIONSTRINGREF = "ATTRIBUTE-DEFINITION-STRING-REF";
    private static final QName QNAME_DEFINITION = new QName("DEFINITION");

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public AttributeValueStringSerializer() {
        super(AttributeValueString.class);
    }

    @Override
    public void serialize(AttributeValueString value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_ATTRIBUTEVALUESTRING);
        generator.writeStartObject();
        identifiable.accept(value, generator);

        generator.setNextIsAttribute(true);
        generator.writeStringProperty(PROPERTY_THEVALUE, value.getTheValue());

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);
        generator.writeStringProperty(PROPERTY_ATTRIBUTEDEFININITIONSTRINGREF, value.getDefinition().getType().getIdentifier());
        generator.finishWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);

        generator.writeEndObject();
    }
}
