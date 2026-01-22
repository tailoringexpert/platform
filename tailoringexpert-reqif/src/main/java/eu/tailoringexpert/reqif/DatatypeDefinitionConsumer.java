package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinition;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionEnumeration;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
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

        generator.finishWrappedValue(name, name);
    }
}
