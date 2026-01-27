package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.*;
import lombok.extern.log4j.Log4j2;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

@Log4j2
public class SpecTypeConsumer implements BiConsumer<SpecType, ToXmlGenerator> {

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();
    private BiConsumer<AttributeDefinition, ToXmlGenerator> attributeDefinition = new AttributeDefinitionConsumer();

    private final Map<Class<?>, BiConsumer<SpecType, ToXmlGenerator>> consumer = ofEntries(
        entry(SpecObjectType.class, (specType, generator) -> {
            identifiable.accept(specType, generator);
            generator.setNextIsAttribute(false);

            QName name = new QName("SPEC-ATTRIBUTES");
            generator.startWrappedValue(name, name);

            specType.getSpecAttributes()
                .forEach(attribute -> attributeDefinition.accept(attribute, generator));

            generator.setNextIsAttribute(false);
            generator.finishWrappedValue(name, name);
        }),
        entry(SpecificationType.class, (specType, generator) ->
            identifiable.accept(specType, generator)
        )
    );

    @Override
    public void accept(SpecType specType, ToXmlGenerator generator) {
        consumer.getOrDefault(specType.getClass(), (value, gen) -> log.debug("no consumer for {}", value.getClass()))
            .accept(specType, generator);
    }

}

