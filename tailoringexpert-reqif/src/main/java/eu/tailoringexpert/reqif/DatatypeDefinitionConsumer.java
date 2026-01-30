package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.*;
import eu.tailoringexpert.serializer.IdentifiableConsumer;
import lombok.extern.log4j.Log4j2;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static java.util.Optional.ofNullable;

@Log4j2
public class DatatypeDefinitionConsumer implements BiConsumer<DatatypeDefinition, ToXmlGenerator> {
    private static final QName QNAME_DATATYPEDEFINITIONSTRING = new QName("DATATYPE-DEFINITION-STRING");
    private static final QName QNAME_DATATYPEDEFINITIONBOOLEAN = new QName("DATATYPE-DEFINITION-BOOLEAN");
    private static final QName QNAME_DATATYPEDEFINITIONENUMERATION = new QName("DATATYPE-DEFINITION-ENUMERATION");
    private static final QName QNAME_SPECIFIEDVALUES = new QName("SPECIFIED-VALUES");
    private static final QName QNAME_ENUMVALUE = new QName("ENUM-VALUE");
    private static final QName QNAME_PROPERTIES = new QName("PROPERTIES");
    private static final QName QNAME_EMBEDDEDVALUE = new QName("EMBEDDED-VALUE");
    private static final String PROPERTY_OTHERCONTENT = "OTHER-CONTENT";
    private static final String PROPERTY_KEY = "KEY";

    BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();

    private final Map<Class<?>, BiConsumer<DatatypeDefinition, ToXmlGenerator>> consumer = Map.ofEntries(
        entry(DatatypeDefinitionString.class, (datatypeDefinition, generator) -> {
            generator.startWrappedValue(QNAME_DATATYPEDEFINITIONSTRING, QNAME_DATATYPEDEFINITIONSTRING);
            identifiable.accept(datatypeDefinition, generator);

            generator.finishWrappedValue(QNAME_DATATYPEDEFINITIONSTRING, QNAME_DATATYPEDEFINITIONSTRING);
        }),
        entry(DatatypeDefinitionBoolean.class, (datatypeDefinition, generator) -> {
            generator.startWrappedValue(QNAME_DATATYPEDEFINITIONBOOLEAN, QNAME_DATATYPEDEFINITIONBOOLEAN);
            identifiable.accept(datatypeDefinition, generator);

            generator.finishWrappedValue(QNAME_DATATYPEDEFINITIONBOOLEAN, QNAME_DATATYPEDEFINITIONBOOLEAN);
        }),
        entry(DatatypeDefinitionEnumeration.class, (datatypeDefinition, generator) -> {
            generator.startWrappedValue(QNAME_DATATYPEDEFINITIONENUMERATION, QNAME_DATATYPEDEFINITIONENUMERATION);
            identifiable.accept(datatypeDefinition, generator);

            generator.startWrappedValue(QNAME_SPECIFIEDVALUES, QNAME_SPECIFIEDVALUES);

            ((DatatypeDefinitionEnumeration) datatypeDefinition).getSpecifiedValues().forEach(value -> {
                generator.setNextIsAttribute(false);
                generator.startWrappedValue(QNAME_ENUMVALUE, QNAME_ENUMVALUE);
                generator.setNextIsAttribute(true);
                this.identifiable.accept(value, generator);
                generator.startWrappedValue(QNAME_PROPERTIES, QNAME_PROPERTIES);

                generator.startWrappedValue(QNAME_EMBEDDEDVALUE, QNAME_EMBEDDEDVALUE);
                generator.setNextIsAttribute(true);
                generator.writeNumberProperty(PROPERTY_KEY, value.getProperties().getKey());
                generator.writeStringProperty(PROPERTY_OTHERCONTENT, value.getProperties().getOtherContent());
                generator.finishWrappedValue(QNAME_EMBEDDEDVALUE,QNAME_EMBEDDEDVALUE);

                generator.finishWrappedValue(QNAME_PROPERTIES, QNAME_PROPERTIES);
                generator.finishWrappedValue(QNAME_ENUMVALUE, QNAME_ENUMVALUE);
            });

            generator.finishWrappedValue(QNAME_SPECIFIEDVALUES, QNAME_SPECIFIEDVALUES);
            generator.finishWrappedValue(QNAME_DATATYPEDEFINITIONENUMERATION, QNAME_DATATYPEDEFINITIONENUMERATION);
        })
    );

    @Override
    public void accept(DatatypeDefinition datatypeDefinition, ToXmlGenerator generator) {
        ofNullable(consumer.get(datatypeDefinition.getClass()))
            .orElseThrow(() -> new NoSuchElementException("No implementation for " + datatypeDefinition.getClass() + " available"))
            .accept(datatypeDefinition, generator);
    }
}
