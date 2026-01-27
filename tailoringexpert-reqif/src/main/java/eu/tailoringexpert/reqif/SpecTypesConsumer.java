package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import lombok.extern.log4j.Log4j2;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static java.util.Optional.ofNullable;

@Log4j2
public class SpecTypesConsumer implements BiConsumer<Collection<SpecType>, ToXmlGenerator> {

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();
    private BiConsumer<SpecType, ToXmlGenerator> specType = new SpecTypeConsumer();

    private final Map<Class<?>, BiConsumer<SpecType, ToXmlGenerator>> consumer = Map.ofEntries(
        entry(SpecObjectType.class, (value, generator) -> {
            QName name = new QName("SPEC-OBJECT-TYPE");
            generator.startWrappedValue(name, name);

            this.identifiable.accept(value, generator);
            this.specType.accept(value, generator);

            generator.finishWrappedValue(name, name);
        }),
        entry(SpecificationType.class, (value, generator) -> {
            QName name = new QName("SPECIFICATION-TYPE");
            generator.startWrappedValue(name, name);

            this.identifiable.accept(value, generator);
            this.specType.accept(value, generator);

            generator.finishWrappedValue(name, name);
        })
    );


    @Override
    public void accept(Collection<SpecType> specTypes, ToXmlGenerator generator) {
        generator.startWrappedValue(new QName("SPEC-TYPES"), new QName("SPEC-TYPES"));
        generator.setNextIsAttribute(true);

        specTypes.forEach(value ->
            ofNullable(consumer.get(value.getClass()))
                .orElseThrow()
                .accept(value, generator)
        );
    }
}
