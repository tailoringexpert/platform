package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.reqif.IdentifiableConsumer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class SpecObjectSerializer extends StdSerializer<SpecObject> {

    private static final QName QNAME_SPECOBJECT = new QName("SPEC-OBJECT");
    private static final QName QNAME_TYPE = new QName("TYPE");
    private static final QName QNAME_VALUES = new QName("VALUES");
    private static final String PROPERTY_SPECTOBJECTTYPREF = "SPEC-OBJECT-TYPE-REF";
    private static final String PROPERTY_VALUES = "values";

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public SpecObjectSerializer() {
        super(SpecObject.class);
    }

    @Override
    public void serialize(SpecObject value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_SPECOBJECT);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(QNAME_TYPE, QNAME_TYPE);
        generator.writeStringProperty(PROPERTY_SPECTOBJECTTYPREF, value.getType().getIdentifier());
        generator.finishWrappedValue(QNAME_TYPE, QNAME_TYPE);

        generator.startWrappedValue(QNAME_VALUES, QNAME_VALUES);
        provider.defaultSerializeProperty(PROPERTY_VALUES, value.getValues(), generator);
        generator.finishWrappedValue(QNAME_VALUES, QNAME_VALUES);

        generator.writeEndObject();
    }
}
