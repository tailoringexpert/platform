package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
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

public class AttributeDefinitionBooleanSerializer extends StdSerializer<AttributeDefinitionBoolean> {
    private static final QName QNAME_ATTRIBUTEDEFINITIONBOOLEAN = new QName("ATTRIBUTE-DEFINITION-BOOLEAN");

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public AttributeDefinitionBooleanSerializer() {
        super(AttributeDefinitionBoolean.class);
    }

    @Override
    public void serialize(AttributeDefinitionBoolean value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_ATTRIBUTEDEFINITIONBOOLEAN);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);


        generator.startWrappedValue(new QName("TYPE"), new QName("TYPE"));
        generator.writeStringProperty("DATATYPE-DEFINITION-BOOLEAN-REF", value.getType().getIdentifier());
        generator.finishWrappedValue(new QName("TYPE"), new QName("TYPE"));

        generator.writeEndObject();
    }

}
