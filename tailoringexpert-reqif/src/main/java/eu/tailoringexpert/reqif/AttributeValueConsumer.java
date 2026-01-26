package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeValue;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
import lombok.extern.log4j.Log4j2;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

@Log4j2
public class AttributeValueConsumer implements BiConsumer<AttributeValue, ToXmlGenerator> {

    private Map<Class<?>, BiConsumer<AttributeValue, ToXmlGenerator>> consumer = ofEntries(
        entry(AttributeValueString.class, (value, generator) -> {
            QName name = new QName("ATTRIBUTE-VALUE-STRING");
            generator.startWrappedValue(name, name);

            generator.setNextIsAttribute(true);
            generator.writeStringProperty("THE-VALUE", ((AttributeValueString) value).getTheValue());

            generator.setNextIsAttribute(false);
            generator.startWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
            generator.writeStringProperty("ATTRIBUTE-DEFINITION-STRING-REF", value.getDefinition().getType().getIdentifier());
            generator.finishWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));

            generator.finishWrappedValue(name, name);
        }),
        entry(AttributeValueBoolean.class, (value, generator) -> {
            QName name = new QName("ATTRIBUTE-VALUE-BOOLEAN");
            generator.startWrappedValue(name, name);

            generator.setNextIsAttribute(true);
            generator.writeBooleanProperty("THE-VALUE", ((AttributeValueBoolean) value).getTheValue());

            generator.setNextIsAttribute(false);
            generator.startWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
            generator.writeStringProperty("ATTRIBUTE-DEFINITION-BOOLEAN-REF", value.getDefinition().getType().getIdentifier());
            generator.finishWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));

            generator.finishWrappedValue(name, name);
        }),
        entry(AttributeValueEnumeration.class, (value, generator) -> {
            QName name = new QName("ATTRIBUTE-VALUE-ENUMERATION");
            generator.startWrappedValue(name, name);

            generator.startWrappedValue(new QName("VALUES"), new QName("VALUES"));
            ((AttributeValueEnumeration) value).getValues()
                .forEach(ref -> generator.writeStringProperty("ENUM-VALUE-REF", ref.getIdentifier()));
            generator.finishWrappedValue(new QName("VALUES"), new QName("VALUES"));

            generator.startWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));
            generator.writeStringProperty("ATTRIBUTE-DEFINITION-ENUMERATION-REF", value.getDefinition().getIdentifier());
            generator.finishWrappedValue(new QName("DEFINITION"), new QName("DEFINITION"));

            generator.finishWrappedValue(name, name);
        })
    );

    @Override
    public void accept(AttributeValue attributeValue, ToXmlGenerator generator) {
        consumer.getOrDefault(attributeValue.getClass(), (value, gen) -> log.debug("no consumer for {]", value.getClass()))
            .accept(attributeValue, generator);
    }

}
