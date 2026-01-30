package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.DatatypeDefinitionEnumeration;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class DatatypeDefinitionEnumerationSerializer extends StdSerializer<DatatypeDefinitionEnumeration> {

    private static final QName QNAME_DATATYPEDEFINITION = new QName("DATATYPE-DEFINITION-ENUMERATION");
    private static final QName QNAME_SPECIFIEDVALUES = new QName("SPECIFIED-VALUES");
    private static final QName QNAME_ENUMVALUE = new QName("ENUM-VALUE");
    private static final QName QNAME_PROPERTIES = new QName("PROPERTIES");
    private static final QName QNAME_EMBEDDEDVALUE = new QName("EMBEDDED-VALUE");
    private static final String PROPERTY_OTHERCONTENT = "OTHER-CONTENT";
    private static final String PROPERTY_KEY = "KEY";

    BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public DatatypeDefinitionEnumerationSerializer() {
        super(DatatypeDefinitionEnumeration.class);
    }

    @Override
    public void serialize(DatatypeDefinitionEnumeration value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_DATATYPEDEFINITION);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);

        generator.startWrappedValue(QNAME_SPECIFIEDVALUES, QNAME_SPECIFIEDVALUES);

        value.getSpecifiedValues().forEach(current -> {
            generator.setNextIsAttribute(false);
            generator.startWrappedValue(QNAME_ENUMVALUE, QNAME_ENUMVALUE);
            generator.setNextIsAttribute(true);
            this.identifiable.accept(current, generator);
            generator.startWrappedValue(QNAME_PROPERTIES, QNAME_PROPERTIES);

            generator.startWrappedValue(QNAME_EMBEDDEDVALUE, QNAME_EMBEDDEDVALUE);
            generator.setNextIsAttribute(true);
            generator.writeNumberProperty(PROPERTY_KEY, current.getProperties().getKey());
            generator.writeStringProperty(PROPERTY_OTHERCONTENT, current.getProperties().getOtherContent());
            generator.finishWrappedValue(QNAME_EMBEDDEDVALUE,QNAME_EMBEDDEDVALUE);

            generator.finishWrappedValue(QNAME_PROPERTIES, QNAME_PROPERTIES);
            generator.finishWrappedValue(QNAME_ENUMVALUE, QNAME_ENUMVALUE);
        });

        generator.finishWrappedValue(QNAME_SPECIFIEDVALUES, QNAME_SPECIFIEDVALUES);
        generator.writeEndObject();

    }
}
