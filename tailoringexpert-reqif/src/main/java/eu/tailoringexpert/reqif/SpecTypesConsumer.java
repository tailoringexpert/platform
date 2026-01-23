package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;

public class SpecTypesConsumer implements BiConsumer<Collection<SpecType>, ToXmlGenerator> {

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();
    private BiConsumer<SpecType, ToXmlGenerator> specType = new SpecTypeConsumer();

    private Map<Class, String> specType2Xml = Map.ofEntries(
        entry(SpecObjectType.class, "SPEC-OBJECT-TYPE"),
        entry(SpecificationType.class, "SPECIFICATION-TYPE")
    );

    @Override
    public void accept(Collection<SpecType> specTypes, ToXmlGenerator generator) {
        generator.startWrappedValue(new QName("", "SPEC-TYPES"), new QName("", "SPEC-TYPES"));
        generator.setNextIsAttribute(true);

        specTypes.forEach(specType -> {
            QName name = new QName("", specType2Xml.get(specType.getClass()));
            generator.startWrappedValue(name, name);

            this.identifiable.accept(specType, generator);
            this.specType.accept(specType, generator);

            generator.finishWrappedValue(name, name);
        });
    }
}
