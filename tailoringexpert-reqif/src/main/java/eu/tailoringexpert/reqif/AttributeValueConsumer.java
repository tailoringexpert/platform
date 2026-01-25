package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeValue;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.function.BiConsumer;

public class AttributeValueConsumer implements BiConsumer<AttributeValue, ToXmlGenerator> {

    private Map<Class, String> attribute2Xml = Map.ofEntries(
        Map.entry(AttributeValueString.class, "ATTRIBUTE-VALUE-STRING")
    );


    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    @Override
    public void accept(AttributeValue attributeValue, ToXmlGenerator generator) {
        QName name = new QName(attribute2Xml.get(attributeValue.getClass()));
        generator.startWrappedValue(name, name);
        generator.setNextIsAttribute(true);
//        identifiable.accept(attributeValue, generator);
//        generator.writeStartObject();

        if (AttributeValueString.class.equals(attributeValue.getClass())) {
            accept((AttributeValueString) attributeValue, generator);
        }

//        generator.writeEndObject();
        generator.finishWrappedValue(name, name);

    }

    private void accept(AttributeValueString valueString, ToXmlGenerator generator) {
        generator.setNextIsAttribute(true);
        generator.writeStringProperty("THE-VALUE", valueString.getTheValue());

        generator.setNextIsAttribute(false);
        generator.startWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
        generator.writeStringProperty("ATTRIBUTE-DEFINITION-STRING-REF", valueString.getDefinition().getType().getIdentifier());

        generator.finishWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
    }

}
