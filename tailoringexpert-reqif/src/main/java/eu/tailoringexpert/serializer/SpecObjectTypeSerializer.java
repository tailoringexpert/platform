package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObjectType;
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
public class SpecObjectTypeSerializer extends StdSerializer<SpecObjectType> {

    private static final QName QNAME_SPECTOBJECTTYPE = new QName("SPEC-OBJECT-TYPE");
    private static final QName QNAME_SPECATTRIBUTES = new QName("SPEC-ATTRIBUTES");
    private static final String PROPERTY_SPEC_ATTRIBUTES = "specAttributes";

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public SpecObjectTypeSerializer() {
        super(SpecObjectType.class);
    }


    @Override
    public void serialize(SpecObjectType value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_SPECTOBJECTTYPE);
        generator.writeStartObject();
        identifiable.accept(value, generator);

        generator.startWrappedValue(QNAME_SPECATTRIBUTES, QNAME_SPECATTRIBUTES);
        provider.defaultSerializeProperty(PROPERTY_SPEC_ATTRIBUTES, value.getSpecAttributes(), generator);
        generator.finishWrappedValue(QNAME_SPECATTRIBUTES, QNAME_SPECATTRIBUTES);

        generator.writeEndObject();
    }
}

