package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.*;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;

public class DatatypeDefinitionConsumer implements BiConsumer<DatatypeDefinition, ToXmlGenerator> {

    BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    private Map<Class, String> datatype2Xml = Map.ofEntries(
        entry(DatatypeDefinitionString.class, "DATATYPE-DEFINITION-STRING"),
        entry(DatatypeDefinitionBoolean.class, "DATATYPE-DEFINITION-BOOLEAN"),
        entry(DatatypeDefinitionEnumeration.class, "DATATYPE-DEFINITION-ENUMERATION")
    );

    @Override
    public void accept(DatatypeDefinition datatypeDefinition, ToXmlGenerator generator) {
        QName name = new QName("", datatype2Xml.get(datatypeDefinition.getClass()));
        generator.startWrappedValue(name, name);

        identifiable.accept(datatypeDefinition, generator);

        if (DatatypeDefinitionEnumeration.class.equals(datatypeDefinition.getClass())) {
            accept((DatatypeDefinitionEnumeration) datatypeDefinition, generator);
        }


        generator.finishWrappedValue(name, name);
    }

    private void accept(DatatypeDefinitionEnumeration datatypeDefinitionEnumeration, ToXmlGenerator generator) {
        generator.startWrappedValue(new QName("SPECIFIED-VALUES"), new QName("SPECIFIED-VALUES"));
        generator.setNextIsAttribute(true);

        datatypeDefinitionEnumeration.getSpecifiedValues().forEach(value -> {
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
    }
}
