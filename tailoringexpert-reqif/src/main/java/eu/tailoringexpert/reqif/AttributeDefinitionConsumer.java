package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.Identifiable;
import lombok.extern.log4j.Log4j2;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.Optional.ofNullable;

@Log4j2

public class AttributeDefinitionConsumer implements BiConsumer<AttributeDefinition, ToXmlGenerator> {

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    private final Map<Class<?>, BiConsumer<AttributeDefinition, ToXmlGenerator>> consumer = ofEntries(
        entry(AttributeDefinitionString.class, (attributeDefinition, generator) -> {
            QName name = new QName("ATTRIBUTE-DEFINITION-STRING");
            generator.startWrappedValue(name, name);
            identifiable.accept(attributeDefinition, generator);

            generator.startWrappedValue(new QName("TYPE"), new QName("TYPE"));
            generator.writeStringProperty("DATATYPE-DEFINITION-STRING-REF", attributeDefinition.getType().getIdentifier());
            generator.finishWrappedValue(new QName("TYPE"), new QName("TYPE"));

            generator.finishWrappedValue(name, name);
        }),
        entry(AttributeDefinitionBoolean.class, (attributeDefinition, generator) -> {
            QName name = new QName("ATTRIBUTE-DEFINITION-BOOLEAN");
            generator.startWrappedValue(name, name);
            identifiable.accept(attributeDefinition, generator);

            generator.startWrappedValue(new QName("TYPE"), new QName("TYPE"));
            generator.writeStringProperty("DATATYPE-DEFINITION-BOOLEAN-REF", attributeDefinition.getType().getIdentifier());
            generator.finishWrappedValue(new QName("TYPE"), new QName("TYPE"));

            generator.finishWrappedValue(name, name);
        })
    );

    @Override
    public void accept(AttributeDefinition attributeDefinition, ToXmlGenerator generator) {
        ofNullable(consumer.get(attributeDefinition.getClass()))
            .orElseThrow(() -> new NoSuchElementException("No implementation for " + attributeDefinition.getClass() + " available"))
            .accept(attributeDefinition, generator);
    }
}
