package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeValue;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.serializer.IdentifiableConsumer;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class SpecObjectConsumer implements BiConsumer<SpecObject, ToXmlGenerator> {

    private static final QName QNAME_SPECOBJECT = new QName("SPEC-OBJECT");
    private static final QName QNAME_TYPE = new QName("TYPE");
    private static final QName QNAME_VALUES = new QName("VALUES");
    private static final String PROPERTY_SPECTOBJECTTYPREF = "SPEC-OBJECT-TYPE-REF";

    private final BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();
    private final BiConsumer<AttributeValue, ToXmlGenerator> attributeValue = new AttributeValueConsumer();

    @Override
    public void accept(SpecObject specObject, ToXmlGenerator generator) {
        generator.startWrappedValue(QNAME_SPECOBJECT, QNAME_SPECOBJECT);
        identifiable.accept(specObject, generator);

        generator.startWrappedValue(QNAME_TYPE, QNAME_TYPE);
        generator.writeStringProperty(PROPERTY_SPECTOBJECTTYPREF, specObject.getType().getIdentifier());
        generator.finishWrappedValue(QNAME_TYPE, QNAME_TYPE);

        generator.startWrappedValue(QNAME_VALUES, QNAME_VALUES);
        specObject.getValues()
            .forEach(value -> attributeValue.accept(value, generator));
        generator.finishWrappedValue(QNAME_VALUES, QNAME_VALUES);

        generator.finishWrappedValue(QNAME_SPECOBJECT, QNAME_SPECOBJECT);
    }
}
