package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeValueBoolean;
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

@Log4j2
public class AttributeValueBooleanSerializer extends StdSerializer<AttributeValueBoolean> {

    private static final QName QNAME_DEFINITION = new QName("DEFINITION");
    private static final QName QNAME_ATTRIBUTEVALUEBOOLEAN = new QName("ATTRIBUTE-VALUE-BOOLEAN");
    private static final String PROPERTY_ATTRIBUTEDEFININITIONBOOLEANREF = "ATTRIBUTE-DEFINITION-BOOLEAN-REF";
    private static final String PROPERTY_THEVALUE = "THE-VALUE";

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public AttributeValueBooleanSerializer() {
        super(AttributeValueBoolean.class);
    }

    @Override
    public void serialize(AttributeValueBoolean value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_ATTRIBUTEVALUEBOOLEAN);
        generator.writeStartObject();
        identifiable.accept(value, generator);

        generator.setNextIsAttribute(true);
        generator.writeBooleanProperty(PROPERTY_THEVALUE, value.getTheValue());

        generator.setNextIsAttribute(false);
        generator.setNextName(QNAME_DEFINITION);
        generator.startWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);
        generator.writeStringProperty(PROPERTY_ATTRIBUTEDEFININITIONBOOLEANREF, value.getDefinition().getType().getIdentifier());
        generator.finishWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);

        generator.writeEndObject();
    }

}
