package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecRelation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class SpecRelationSerializer extends StdSerializer<SpecRelation> {

    private static final QName QNAME_SPECRELATION = new QName("SPEC-RELATION");
    private static final QName QNAME_SOURCE = new QName("SOURCE");
    private static final QName QNAME_TARGET = new QName("TARGET");
    private static final QName QNAME_TYPE = new QName("TYPE");
    private static final String PROPERTY_SPECOBJECTREF=  "SPEC-OBJECT-REF";
    private static final String PROPERTY_RELATIONTYPEREF = "SPECIFICATION-TYPE-REF";

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public SpecRelationSerializer() {
        super(SpecRelation.class);
    }

    @Override
    public void serialize(SpecRelation value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_SPECRELATION);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(QNAME_TYPE, QNAME_TYPE);
        generator.writeStringProperty(PROPERTY_RELATIONTYPEREF, value.getType().getIdentifier());
        generator.finishWrappedValue(QNAME_TYPE, QNAME_TYPE);

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(QNAME_SOURCE, QNAME_SOURCE);
        generator.writeStringProperty(PROPERTY_SPECOBJECTREF, value.getSource().getIdentifier());
        generator.finishWrappedValue(QNAME_SOURCE, QNAME_SOURCE);

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(QNAME_TARGET, QNAME_TARGET);
        generator.writeStringProperty(PROPERTY_SPECOBJECTREF, value.getTarget().getIdentifier());
        generator.finishWrappedValue(QNAME_TARGET, QNAME_TARGET);

        generator.writeEndObject();
    }
}
