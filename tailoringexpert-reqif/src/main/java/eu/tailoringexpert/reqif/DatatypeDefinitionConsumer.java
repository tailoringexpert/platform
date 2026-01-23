package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinition;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionEnumeration;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Map.entry;

public class DatatypeDefinitionConsumer implements BiConsumer<ToXmlGenerator, DatatypeDefinition> {

    BiConsumer<ToXmlGenerator, Identifiable> identifiable = new IdentifiableConsumer();

    private Map<Class, String> datatype2Xml = Map.ofEntries(
        entry(DatatypeDefinitionString.class, "DATATYPE-DEFINITION-STRING"),
        entry(DatatypeDefinitionBoolean.class, "DATATYPE-DEFINITION-BOOLEAN"),
        entry(DatatypeDefinitionEnumeration.class, "DATATYPE-DEFINITION-ENUMERATION")
    );

    @Override
    public void accept(ToXmlGenerator generator, DatatypeDefinition datatypeDefinition) {
        QName name = new QName("", datatype2Xml.get(datatypeDefinition.getClass()));
        generator.startWrappedValue(name, name);

        identifiable.accept(generator, datatypeDefinition);

        if (DatatypeDefinitionEnumeration.class.equals(datatypeDefinition.getClass())) {
            accept(generator, (DatatypeDefinitionEnumeration) datatypeDefinition);
        }


        generator.finishWrappedValue(name, name);
    }

    private void accept(ToXmlGenerator generator, DatatypeDefinitionEnumeration datatypeDefinitionEnumeration) {
        generator.startWrappedValue(new QName("", "SPECIFIED-VALUES"), new QName("", "SPECIFIED-VALUES"));
        generator.setNextIsAttribute(true);

        datatypeDefinitionEnumeration.getSpecifiedValues().forEach(value -> {
            QName name = new QName("", "ENUM-VALUE");
            generator.startWrappedValue(name, name);
            this.identifiable.accept(generator, value);
            generator.startWrappedValue(new QName("", "EMBEDDED-VALUE"), new QName("", "EMBEDDED-VALUE"));
            generator.setNextIsAttribute(true);
            generator.writeNumberProperty("KEY", value.getProperties().getKey());
            generator.writeStringProperty("OTHER-CONTENT", value.getProperties().getOtherContent());
            generator.finishWrappedValue(new QName("", "EMBEDDED-VALUE"), new QName("", "EMBEDDED-VALUE"));
            generator.finishWrappedValue(name, name);
        });

        generator.finishWrappedValue(new QName("", "SPECIFIED-VALUES"), new QName("", "SPECIFIED-VALUES"));
        ;
    }
}
