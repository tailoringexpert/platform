package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.SpecObject;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.function.BiConsumer;

public class SpecObjectsConsumer implements BiConsumer<Collection<SpecObject>, ToXmlGenerator> {

    private BiConsumer<SpecObject, ToXmlGenerator> specObject = new SpecObjectConsumer();

    @Override
    public void accept(Collection<SpecObject> specObjects, ToXmlGenerator generator) {
        QName name = new QName("SPEC-OBJECTS");
        generator.startWrappedValue(name, name);
        generator.setNextIsAttribute(false);

        specObjects
            .forEach(value -> specObject.accept(value, generator));

        generator.finishWrappedValue(name, name);
    }
}
