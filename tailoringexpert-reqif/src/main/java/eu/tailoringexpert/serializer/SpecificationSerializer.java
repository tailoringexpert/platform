package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.Specification;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

public class SpecificationSerializer extends StdSerializer<Specification> {

    private static final QName QNAME_SPECIFICATION = new QName("SPECIFICATION");
    private static final QName QNAME_TYPE = new QName("TYPE");
    private static final QName QNAME_CHILDREN = new QName("CHILDREN");
    private static final QName QNAME_OBJECT = new QName("OBJECT");
    private static final QName QNAME_SPECHIERARCHY = new QName("SPEC-HIERARCHY");
    private static final String PROPERTY_SPECIFICATIONTYPEREF = "SPECIFICATION-TYPE-REF";
    private static final String PROPERTY_SPECOBJECTREF = "SPEC-OBJECT-REF";

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    public SpecificationSerializer() {
        super(Specification.class);
    }

    @Override
    public void serialize(Specification value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        ToXmlGenerator generator = (ToXmlGenerator) gen;

        generator.setNextName(QNAME_SPECIFICATION);
        generator.writeStartObject();
        generator.setNextIsAttribute(true);
        identifiable.accept(value, generator);

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(QNAME_TYPE, QNAME_TYPE);
        generator.writeStringProperty(PROPERTY_SPECIFICATIONTYPEREF, value.getType().getIdentifier());
        generator.finishWrappedValue(QNAME_TYPE, QNAME_TYPE);

        ofNullable(value.getChildren())
            .ifPresent(hierarchies -> {
                generator.startWrappedValue(QNAME_CHILDREN, QNAME_CHILDREN);

                hierarchies.forEach(hierarchy -> {
                        generator.startWrappedValue(QNAME_SPECHIERARCHY, QNAME_SPECHIERARCHY);
                        generator.setNextIsAttribute(true);
                        identifiable.accept(hierarchy, generator);

                        generator.startWrappedValue(QNAME_OBJECT, QNAME_OBJECT);
                        generator.writeStringProperty(PROPERTY_SPECOBJECTREF, hierarchy.getObject().getIdentifier());
                        generator.finishWrappedValue(QNAME_OBJECT, QNAME_OBJECT);
                    }
                );

                generator.finishWrappedValue(QNAME_CHILDREN, QNAME_CHILDREN);
            });

        generator.writeEndObject();
    }
}
