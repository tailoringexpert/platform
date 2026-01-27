package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

public class IdentifiableConsumer implements BiConsumer<Identifiable, ToXmlGenerator> {

    @Override
    public void accept(Identifiable identifiable, ToXmlGenerator generator) {
        generator.setNextIsAttribute(true);
        generator.writeStringProperty("IDENTIFIER", identifiable.getIdentifier());
        generator.writeStringProperty("LONG-NAME", identifiable.getLongName());
        ofNullable(identifiable.getLastChange())
            .ifPresent(lastChange -> generator.writeStringProperty("LAST-CHANGE", lastChange.toString()));
        generator.setNextIsAttribute(false);
    }
}
