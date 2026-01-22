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

public class AttributeDefinitionConsumer implements BiConsumer<ToXmlGenerator, AttributeDefinition> {

    private BiConsumer<ToXmlGenerator, Identifiable> identifiable = new IdentifiableConsumer();

    private Map<Class, String> attributeDefinition2Xml = Map.ofEntries(
        entry(AttributeDefinitionString.class, "ATTRIBUTE-DEFINITION-STRING"),
        entry(AttributeDefinitionBoolean.class, "ATTRIBUTE-DEFINITION-BOOLEAN")
    );

    private Map<Class, String> attributeDefinitionRef2Xml = Map.ofEntries(
        entry(AttributeDefinitionString.class, "DATATYPE-DEFINITION-STRING-REF"),
        entry(AttributeDefinitionBoolean.class, "DATATYPE-DEFINITION-BOOLEAN-REF")
    );

    @Override
    public void accept(ToXmlGenerator generator, AttributeDefinition attributeDefinition) {
        QName name = new QName("", attributeDefinition2Xml.get(attributeDefinition.getClass()));

        generator.startWrappedValue(name, name);
        identifiable.accept(generator, attributeDefinition);
        //writeIdentifiables(xml, attributeDefinition);

        // type
        generator.startWrappedValue(new QName("", "TYPE"), new QName("", "TYPE"));
        generator.writeStringProperty(attributeDefinitionRef2Xml.get(attributeDefinition.getClass()), attributeDefinition.getType().getIdentifier());
        generator.finishWrappedValue(new QName("", "TYPE"), new QName("", "TYPE"));

        generator.finishWrappedValue(name, name);
    }
}
