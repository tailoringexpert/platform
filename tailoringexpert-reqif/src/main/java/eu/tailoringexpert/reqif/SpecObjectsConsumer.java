package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.SpecObject;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.function.BiConsumer;

public class SpecObjectsConsumer implements BiConsumer<Collection<SpecObject>, ToXmlGenerator> {

    private static final QName QNAME_SPECOBJECTS = new QName("SPEC-OBJECTS");

    private BiConsumer<SpecObject, ToXmlGenerator> specObject = new SpecObjectConsumer();

    @Override
    public void accept(Collection<SpecObject> specObjects, ToXmlGenerator generator) {
        generator.startWrappedValue(QNAME_SPECOBJECTS, QNAME_SPECOBJECTS);
        specObjects.forEach(value -> specObject.accept(value, generator));
        generator.finishWrappedValue(QNAME_SPECOBJECTS, QNAME_SPECOBJECTS);
    }
}
