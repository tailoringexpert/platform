package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinition;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.function.BiConsumer;

public class DatatypeDefinitionsConsumer implements BiConsumer<Collection<DatatypeDefinition>, ToXmlGenerator> {

    private BiConsumer<DatatypeDefinition, ToXmlGenerator> datatype = new DatatypeDefinitionConsumer();

    @Override
    public void accept(Collection<DatatypeDefinition> datatypeDefinitions, ToXmlGenerator generator) {
        QName name = new QName("DATATYPES");
        generator.startWrappedValue(name, name);
        generator.setNextIsAttribute(false);

        datatypeDefinitions
            .forEach(datatypeDefinition -> datatype.accept(datatypeDefinition, generator));

        generator.finishWrappedValue(name, name);
    }
}
