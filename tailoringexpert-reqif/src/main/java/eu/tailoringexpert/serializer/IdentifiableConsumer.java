package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.Identifiable;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

public class IdentifiableConsumer implements BiConsumer<Identifiable, ToXmlGenerator> {

    private static final String PROPERTY_IDENTIFIER = "IDENTIFIER";
    private static final String PROPERTY_LONGNAME = "LONG-NAME";
    private static final String PROPERTY_LASTCHANGE = "LAST-CHANGE";

    @Override
    public void accept(Identifiable identifiable, ToXmlGenerator generator) {
        generator.setNextIsAttribute(true);
        generator.writeStringProperty(PROPERTY_IDENTIFIER, identifiable.getIdentifier());
        generator.writeStringProperty(PROPERTY_LONGNAME, identifiable.getLongName());
        ofNullable(identifiable.getLastChange())
            .ifPresent(lastChange -> generator.writeStringProperty(PROPERTY_LASTCHANGE, lastChange.toString()));
        generator.setNextIsAttribute(false);
    }
}
