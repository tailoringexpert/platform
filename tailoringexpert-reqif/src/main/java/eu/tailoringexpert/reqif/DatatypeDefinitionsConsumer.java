package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinition;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.function.BiConsumer;

public class DatatypeDefinitionsConsumer implements BiConsumer<ToXmlGenerator, Collection<DatatypeDefinition>> {

    private BiConsumer<ToXmlGenerator, DatatypeDefinition> datatype = new DatatypeDefinitionConsumer();

    @Override
    public void accept(ToXmlGenerator generator, Collection<DatatypeDefinition> datatypeDefinitions) {
        generator.startWrappedValue(new QName("", "DATATYPES"), new QName("", "DATATYPES"));
        generator.setNextIsAttribute(false);

        datatypeDefinitions
            .forEach(datatypeDefinition -> datatype.accept(generator, datatypeDefinition));

        generator.finishWrappedValue(null, new QName("", "DATATYPES"));
    }
}
