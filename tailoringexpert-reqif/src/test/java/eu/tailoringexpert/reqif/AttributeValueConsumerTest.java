package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValue;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.EnumValue;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeValueConsumerTest {
    XmlMapper xmlMapper;
    XMLOutputFactory xmlFactory;
    AttributeValueConsumer consumer;

    @BeforeEach
    void setup() {
        this.xmlMapper = XmlMapper.builder().build();
        this.xmlFactory = this.xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory();
        this.consumer = new AttributeValueConsumer();
    }

    @Test
    void accept_UnknownType_NoSuchElementExceptionThrown() throws XMLStreamException, IOException {
        // arrange
        UnKnownAttributeValue attributeValue = UnKnownAttributeValue.builder().build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory()
            .createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        Throwable actual = Assertions.catchThrowable(() -> consumer.accept(attributeValue, generator));

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void accept_AttributeValueString_AttributeValueStringConsumed() throws XMLStreamException, IOException {
        // arrange
        AttributeValueString attributeValue = AttributeValueString.builder()
            .theValue("Requirement")
            .definition(AttributeDefinitionString.builder()
                .type(DatatypeDefinitionString.builder()
                    .identifier("a-puid")
                    .longName("PUID")
                    .build())
                .build())
            .build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory()
            .createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(attributeValue, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw.toString())
            .isNotEmpty()
            .isEqualTo("<test><ATTRIBUTE-VALUE-STRING THE-VALUE=\"Requirement\"><DEFINITION><ATTRIBUTE-DEFINITION-STRING-REF>a-puid</ATTRIBUTE-DEFINITION-STRING-REF></DEFINITION></ATTRIBUTE-VALUE-STRING></test>");
    }

    @Test
    void accept_AttributeValueBoolean_AttributeValueBooleanConsumed() throws XMLStreamException, IOException {
        // arrange
        AttributeValueBoolean attributeValue = AttributeValueBoolean.builder()
            .theValue(true)
            .definition(AttributeDefinitionBoolean.builder()
                .type(DatatypeDefinitionBoolean.builder()
                    .identifier("a-active")
                    .longName("Active")
                    .build())
                .build())
            .build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory()
            .createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(attributeValue, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw.toString())
            .isNotEmpty()
            .isEqualTo("<test><ATTRIBUTE-VALUE-BOOLEAN THE-VALUE=\"true\"><DEFINITION><ATTRIBUTE-DEFINITION-BOOLEAN-REF>a-active</ATTRIBUTE-DEFINITION-BOOLEAN-REF></DEFINITION></ATTRIBUTE-VALUE-BOOLEAN></test>");
    }

    @Test
    void accept_AttributeValueEnumeration_AttributeValueEnumerationConsumed() throws XMLStreamException, IOException {
        // arrange
        AttributeValueEnumeration attributeValue = AttributeValueEnumeration.builder()
            .values(List.of(
                EnumValue.builder()
                    .identifier("v-ecss-q-st-80d software product assurance")
                    .build()
            ))
            .definition(AttributeDefinitionEnumeration.builder()
                .identifier("a-type")
                .build())
            .build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory()
            .createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(attributeValue, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw.toString())
            .isNotEmpty()
            .isEqualTo("<test><ATTRIBUTE-VALUE-ENUMERATION><VALUES><ENUM-VALUE-REF>v-ecss-q-st-80d software product assurance</ENUM-VALUE-REF></VALUES><DEFINITION><ATTRIBUTE-DEFINITION-ENUMERATION-REF>a-type</ATTRIBUTE-DEFINITION-ENUMERATION-REF></DEFINITION></ATTRIBUTE-VALUE-ENUMERATION></test>");
    }

    @SuperBuilder
    @Getter
    private static class UnKnownAttributeValue extends AttributeValue {
        @Override
        public AttributeDefinition getDefinition() {
            return null;
        }
    }

}
