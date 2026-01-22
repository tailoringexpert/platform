package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.util.function.BiConsumer;

public class IdentifiableConsumer implements BiConsumer<ToXmlGenerator, Identifiable> {

    @Override
    public void accept(ToXmlGenerator generator, Identifiable identifiable) {
        generator.setNextIsAttribute(true);
        generator.writeStringProperty("IDENTIFIER", identifiable.getIdentifier());
//        xml.writeStringProperty("LAST-CHANGE", identifiable.getLastChange().toString());
        generator.writeStringProperty("LONG-NAME", identifiable.getLongName());
        generator.setNextIsAttribute(false);
    }
}
