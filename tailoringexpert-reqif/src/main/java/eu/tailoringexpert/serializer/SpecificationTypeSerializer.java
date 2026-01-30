package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecificationType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class SpecificationTypeSerializer extends StdSerializer<SpecificationType> {

    private static final QName QNAME_SPECIFICATIONTYPE = new QName("SPECIFICATION-TYPE");

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public SpecificationTypeSerializer() {
        super(SpecificationType.class);
    }

    @Override
    public void serialize(SpecificationType value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_SPECIFICATIONTYPE);
        generator.writeStartObject();
        identifiable.accept(value, generator);

        generator.writeEndObject();
    }
}

