package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.Specification;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Optional;
import java.util.function.BiConsumer;

public class SpecificationConsumer implements BiConsumer<Specification, ToXmlGenerator> {
    private static final QName QNAME_SPECIFICATION = new QName("SPECIFICATION");
    private static final QName QNAME_TYPE = new QName("TYPE");
    private static final QName QNAME_CHILDREN = new QName("CHILDREN");
    private static final String PROPERTY_SPECIFICATIONTYPEREF = "SPECIFICATION-TYPE-REF";
    private static final QName QNAME_OBJECT = new QName("OBJECT");
    private static final QName QNAME_SPECHIERARCHY = new QName("SPEC-HIERARCHY");
    private static final String PROPERTY_SPECOBJECTREF = "SPEC-OBJECT-REF";


    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    @Override
    public void accept(Specification specification, ToXmlGenerator generator) {
        generator.startWrappedValue(QNAME_SPECIFICATION, QNAME_SPECIFICATION);
        generator.setNextIsAttribute(true);
        identifiable.accept(specification, generator);

        generator.startWrappedValue(QNAME_TYPE, QNAME_TYPE);
        generator.writeStringProperty(PROPERTY_SPECIFICATIONTYPEREF, specification.getType().getIdentifier());
        generator.finishWrappedValue(QNAME_TYPE, QNAME_TYPE);


        Optional.ofNullable(specification.getChildren())
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


        generator.finishWrappedValue(QNAME_SPECIFICATION, QNAME_SPECIFICATION);
    }
}
