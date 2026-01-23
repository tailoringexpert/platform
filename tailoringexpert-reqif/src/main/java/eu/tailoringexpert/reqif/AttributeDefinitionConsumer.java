package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;

public class AttributeDefinitionConsumer implements BiConsumer<AttributeDefinition, ToXmlGenerator> {

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    private Map<Class, String> attributeDefinition2Xml = Map.ofEntries(
        entry(AttributeDefinitionString.class, "ATTRIBUTE-DEFINITION-STRING"),
        entry(AttributeDefinitionBoolean.class, "ATTRIBUTE-DEFINITION-BOOLEAN")
    );

    private Map<Class, String> attributeDefinitionRef2Xml = Map.ofEntries(
        entry(AttributeDefinitionString.class, "DATATYPE-DEFINITION-STRING-REF"),
        entry(AttributeDefinitionBoolean.class, "DATATYPE-DEFINITION-BOOLEAN-REF")
    );

    @Override
    public void accept(AttributeDefinition attributeDefinition, ToXmlGenerator generator) {
        QName name = new QName("", attributeDefinition2Xml.get(attributeDefinition.getClass()));

        generator.startWrappedValue(name, name);
        identifiable.accept(attributeDefinition, generator);

        // type
        generator.startWrappedValue(new QName("", "TYPE"), new QName("", "TYPE"));
        generator.writeStringProperty(attributeDefinitionRef2Xml.get(attributeDefinition.getClass()), attributeDefinition.getType().getIdentifier());
        generator.finishWrappedValue(new QName("", "TYPE"), new QName("", "TYPE"));

        generator.finishWrappedValue(name, name);
    }
}
