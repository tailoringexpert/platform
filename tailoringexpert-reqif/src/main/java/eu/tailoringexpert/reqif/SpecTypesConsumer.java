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

public class SpecTypesConsumer implements BiConsumer<ToXmlGenerator, Collection<SpecType>> {

    private BiConsumer<ToXmlGenerator, Identifiable> identifiable = new IdentifiableConsumer();
    private BiConsumer<ToXmlGenerator, SpecType> specType = new SpecTypeConsumer();

    private Map<Class, String> specType2Xml = Map.ofEntries(
        entry(SpecObjectType.class, "SPEC-OBJECT-TYPE"),
        entry(SpecificationType.class, "SPECIFICATION-TYPE")
    );

    @Override
    public void accept(ToXmlGenerator generator, Collection<SpecType> specTypes) {
        generator.startWrappedValue(new QName("", "SPECTYPES"), new QName("", "SPECTYPES"));
        generator.setNextIsAttribute(true);

        specTypes.forEach(specType -> {
            QName name = new QName("", specType2Xml.get(specType.getClass()));
            generator.startWrappedValue(name, name);

            this.identifiable.accept(generator, specType);
            this.specType.accept(generator, specType);

            generator.finishWrappedValue(name, name);
        });
    }
}
