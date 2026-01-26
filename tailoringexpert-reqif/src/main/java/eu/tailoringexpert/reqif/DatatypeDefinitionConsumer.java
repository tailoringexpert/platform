package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.*;
import lombok.extern.log4j.Log4j2;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;

@Log4j2
public class DatatypeDefinitionConsumer implements BiConsumer<DatatypeDefinition, ToXmlGenerator> {

    BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    private Map<Class, BiConsumer<DatatypeDefinition, ToXmlGenerator>> consumer = Map.ofEntries(
        entry(DatatypeDefinitionString.class, (datatypeDefinition, generator) -> {
            QName name = new QName("DATATYPE-DEFINITION-STRING");
            generator.startWrappedValue(name, name);
            identifiable.accept(datatypeDefinition, generator);

            generator.finishWrappedValue(name, name);
        }),
        entry(DatatypeDefinitionBoolean.class, (datatypeDefinition, generator) -> {
            QName name = new QName("DATATYPE-DEFINITION-BOOLEAN");
            generator.startWrappedValue(name, name);
            identifiable.accept(datatypeDefinition, generator);

            generator.finishWrappedValue(name, name);
        }),
        entry(DatatypeDefinitionEnumeration.class, (datatypeDefinition, generator) -> {
            QName name = new QName("DATATYPE-DEFINITION-ENUMERATION");
            generator.startWrappedValue(name, name);
            identifiable.accept(datatypeDefinition, generator);

            generator.startWrappedValue(new QName("SPECIFIED-VALUES"), new QName("SPECIFIED-VALUES"));
            //generator.setNextIsAttribute(true);

            ((DatatypeDefinitionEnumeration) datatypeDefinition).getSpecifiedValues().forEach(value -> {
                generator.setNextIsAttribute(false);
                generator.startWrappedValue(new QName("ENUM-VALUE"), new QName("ENUM-VALUE"));
                generator.setNextIsAttribute(true);
                this.identifiable.accept(value, generator);
                generator.startWrappedValue(new QName("PROPERTIES"), new QName("PROPERTIES"));

                generator.startWrappedValue(new QName("EMBEDDED-VALUE"), new QName("EMBEDDED-VALUE"));
                generator.setNextIsAttribute(true);
                generator.writeNumberProperty("KEY", value.getProperties().getKey());
                generator.writeStringProperty("OTHER-CONTENT", value.getProperties().getOtherContent());
                generator.finishWrappedValue(new QName("EMBEDDED-VALUE"), new QName("", "EMBEDDED-VALUE"));

                generator.finishWrappedValue(new QName("PROPERTIES"), new QName("PROPERTIES"));
                generator.finishWrappedValue(new QName("ENUM-VALUE"), new QName("ENUM-VALUE"));
            });

            generator.finishWrappedValue(new QName("SPECIFIED-VALUES"), new QName("SPECIFIED-VALUES"));
            generator.finishWrappedValue(name, name);
        })
    );

    @Override
    public void accept(DatatypeDefinition datatypeDefinition, ToXmlGenerator generator) {
        consumer.getOrDefault(datatypeDefinition.getClass(), (value, gen) -> log.debug("no consumer for {]", value.getClass()))
            .accept(datatypeDefinition, generator);
    }
}
