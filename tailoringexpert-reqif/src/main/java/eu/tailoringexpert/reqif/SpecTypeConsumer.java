package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import eu.tailoringexpert.serializer.IdentifiableConsumer;
import lombok.extern.log4j.Log4j2;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.Optional.ofNullable;

@Log4j2
public class SpecTypeConsumer implements BiConsumer<SpecType, ToXmlGenerator> {

    private static final QName QNAME_SPECTOBJECTTYPE = new QName("SPEC-OBJECT-TYPE");
    private static final QName QNAME_SPECATTRIBUTES = new QName("SPEC-ATTRIBUTES");
    private static final QName QNAME_SPECIFICATIONTYPE = new QName("SPECIFICATION-TYPE");

    private BiConsumer<Identifiable, ToXmlGenerator> identifiable = new IdentifiableConsumer();
    private BiConsumer<AttributeDefinition, ToXmlGenerator> attributeDefinition = new AttributeDefinitionConsumer();

    private final Map<Class<?>, BiConsumer<SpecType, ToXmlGenerator>> consumer = ofEntries(
        entry(SpecObjectType.class, (specType, generator) -> {
            generator.startWrappedValue(QNAME_SPECTOBJECTTYPE, QNAME_SPECTOBJECTTYPE);
            identifiable.accept(specType, generator);
            generator.setNextIsAttribute(false);

            generator.startWrappedValue(QNAME_SPECATTRIBUTES, QNAME_SPECATTRIBUTES);
            specType.getSpecAttributes()
                .forEach(attribute -> attributeDefinition.accept(attribute, generator));
            generator.finishWrappedValue(QNAME_SPECATTRIBUTES, QNAME_SPECATTRIBUTES);

            generator.finishWrappedValue(QNAME_SPECTOBJECTTYPE, QNAME_SPECTOBJECTTYPE);
        }),
        entry(SpecificationType.class, (specType, generator) -> {
            generator.startWrappedValue(QNAME_SPECIFICATIONTYPE, QNAME_SPECIFICATIONTYPE);
            generator.setNextIsAttribute(true);
            identifiable.accept(specType, generator);
            generator.finishWrappedValue(QNAME_SPECIFICATIONTYPE, QNAME_SPECIFICATIONTYPE);
        })
    );

    @Override
    public void accept(SpecType specType, ToXmlGenerator generator) {
        ofNullable(consumer.get(specType.getClass()))
            .orElseThrow(() -> new NoSuchElementException("No implementation for " + specType.getClass() + " available"))
            .accept(specType, generator);
    }

}

