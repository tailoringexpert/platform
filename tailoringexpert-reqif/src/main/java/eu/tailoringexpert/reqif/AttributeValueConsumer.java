package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeValue;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
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
public class AttributeValueConsumer implements BiConsumer<AttributeValue, ToXmlGenerator> {

    private static final QName QNAME_ATTRIBUTEVALUESTRING = new QName("ATTRIBUTE-VALUE-STRING");
    private static final String PROPERTY_THEVALUE = "THE-VALUE";
    private static final String PROPERTY_ATTRIBUTEDEFININITIONSTRINGREF = "ATTRIBUTE-DEFINITION-STRING-REF";
    private static final QName QNAME_DEFINITION = new QName("DEFINITION");

    private static final QName QNAME_ATTRIBUTEVALUEBOOLEAN = new QName("ATTRIBUTE-VALUE-BOOLEAN");
    private static final String PROPERTY_ATTRIBUTEDEFININITIONBOOLEANREF = "ATTRIBUTE-DEFINITION-BOOLEAN-REF";

    private static final QName QNAME_ATTRIBUTEVALUEENUMERATION = new QName("ATTRIBUTE-VALUE-ENUMERATION");
    private static final QName QNAME_VALUES = new QName("VALUES");
    private static final String PROPERTY_ENUMVALUEREF = "ENUM-VALUE-REF";
    private static final String PROPERTY_ATTRIBUTEDEFINITIONENUMERATIONREF = "ATTRIBUTE-DEFINITION-ENUMERATION-REF";

    private final Map<Class<?>, BiConsumer<AttributeValue, ToXmlGenerator>> consumer = ofEntries(
        entry(AttributeValueString.class, (value, generator) -> {
            generator.startWrappedValue(QNAME_ATTRIBUTEVALUESTRING, QNAME_ATTRIBUTEVALUESTRING);

            generator.setNextIsAttribute(true);
            generator.writeStringProperty(PROPERTY_THEVALUE, ((AttributeValueString) value).getTheValue());

            generator.setNextIsAttribute(false);
            generator.startWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);
            generator.writeStringProperty(PROPERTY_ATTRIBUTEDEFININITIONSTRINGREF, value.getDefinition().getType().getIdentifier());
            generator.finishWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);

            generator.finishWrappedValue(QNAME_ATTRIBUTEVALUESTRING, QNAME_ATTRIBUTEVALUESTRING);
        }),
        entry(AttributeValueBoolean.class, (value, generator) -> {
            generator.startWrappedValue(QNAME_ATTRIBUTEVALUEBOOLEAN, QNAME_ATTRIBUTEVALUEBOOLEAN);

            generator.setNextIsAttribute(true);
            generator.writeBooleanProperty(PROPERTY_THEVALUE, ((AttributeValueBoolean) value).getTheValue());

            generator.setNextIsAttribute(false);
            generator.startWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);
            generator.writeStringProperty(PROPERTY_ATTRIBUTEDEFININITIONBOOLEANREF, value.getDefinition().getType().getIdentifier());
            generator.finishWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);

            generator.finishWrappedValue(QNAME_ATTRIBUTEVALUEBOOLEAN, QNAME_ATTRIBUTEVALUEBOOLEAN);
        }),
        entry(AttributeValueEnumeration.class, (value, generator) -> {
            generator.startWrappedValue(QNAME_ATTRIBUTEVALUEENUMERATION, QNAME_ATTRIBUTEVALUEENUMERATION);

            generator.startWrappedValue(QNAME_VALUES, QNAME_VALUES);
            ((AttributeValueEnumeration) value).getValues()
                .forEach(ref -> generator.writeStringProperty(PROPERTY_ENUMVALUEREF, ref.getIdentifier()));
            generator.finishWrappedValue(QNAME_VALUES, QNAME_VALUES);

            generator.startWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);
            generator.writeStringProperty(PROPERTY_ATTRIBUTEDEFINITIONENUMERATIONREF, value.getDefinition().getIdentifier());
            generator.finishWrappedValue(QNAME_DEFINITION, QNAME_DEFINITION);

            generator.finishWrappedValue(QNAME_ATTRIBUTEVALUEENUMERATION, QNAME_ATTRIBUTEVALUEENUMERATION);
        })
    );

    @Override
    public void accept(AttributeValue attributeValue, ToXmlGenerator generator) {
        ofNullable(consumer.get(attributeValue.getClass()))
            .orElseThrow(() -> new NoSuchElementException("No implementation for " + attributeValue.getClass() + " available"))
            .accept(attributeValue, generator);
    }

}
