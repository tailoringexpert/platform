package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeValue;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObject;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.function.BiConsumer;

public class SpecObjectConsumer implements BiConsumer<SpecObject, ToXmlGenerator> {

    BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();
    BiConsumer<AttributeValue, ToXmlGenerator> attributeValue = new AttributeValueConsumer();

    @Override
    public void accept(SpecObject specObject, ToXmlGenerator generator) {
        QName name = new QName("SPEC-OBJECT");
        generator.startWrappedValue(name, name);
        identifiable.accept(specObject, generator);

        generator.startWrappedValue(new QName("TYPE"), new QName("TYPE"));
        generator.writeStringProperty("SPEC-OBJECT-TYPE-REF", specObject.getType().getIdentifier());
        generator.finishWrappedValue(new QName("TYPE"), new QName("TYPE"));

        generator.startWrappedValue(new QName("VALUES"), new QName("VALUES"));
        specObject.getValues()
            .forEach(value -> attributeValue.accept(value, generator));
        generator.finishWrappedValue(new QName("VALUES"), new QName("VALUES"));

        generator.finishWrappedValue(name, name);
    }
}
