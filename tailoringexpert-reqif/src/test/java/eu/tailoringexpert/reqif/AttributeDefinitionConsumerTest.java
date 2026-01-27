package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinition;
import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.DatatypeDefinition;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
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
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeDefinitionConsumerTest {
    XmlMapper xmlMapper;
    XMLOutputFactory xmlFactory;
    AttributeDefinitionConsumer consumer;

    @BeforeEach
    void setup() {
        this.xmlMapper = XmlMapper.builder().build();
        this.xmlFactory = this.xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory();
        this.consumer = new AttributeDefinitionConsumer();
    }

    @Test
    void accept_UnknownType_NoSuchElementExceptionThrown() throws XMLStreamException, IOException {
        // arrange
        UnKnownAttributeDefinition attributeDefinition = UnKnownAttributeDefinition.builder().build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory()
            .createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        Throwable actual = Assertions.catchThrowable(() -> consumer.accept(attributeDefinition, generator));

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void accept_AttributeDefinitionString_AttributeDefintionStringConsumed() throws XMLStreamException, IOException {
        // arrange
        AttributeDefinitionString attributeDefinition = AttributeDefinitionString.builder()
            .identifier("a-document-code")
            .longName("Document Code")
            .type(DatatypeDefinitionString.builder()
                .maxLength(10000)
                .identifier("dt-string")
                .longName("String")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build())
            .build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(attributeDefinition, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw.toString())
            .isNotEmpty()
            .isEqualTo("<test><ATTRIBUTE-DEFINITION-STRING IDENTIFIER=\"a-document-code\" LONG-NAME=\"Document Code\"><TYPE><DATATYPE-DEFINITION-STRING-REF>dt-string</DATATYPE-DEFINITION-STRING-REF></TYPE></ATTRIBUTE-DEFINITION-STRING></test>");
    }

    @Test
    void accept_AttributeDefinitionBoolean_AttributeDefintionBooleanConsumed() throws XMLStreamException, IOException {
        // arrange
        AttributeDefinitionBoolean attributeDefinition = AttributeDefinitionBoolean.builder()
            .identifier("a-active")
            .longName("Active")
            .type(DatatypeDefinitionBoolean.builder()
                .identifier("dt-boolean")
                .longName("Boolean")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build())
            .build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(attributeDefinition, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw.toString())
            .isNotEmpty()
            .isEqualTo("<test><ATTRIBUTE-DEFINITION-BOOLEAN IDENTIFIER=\"a-active\" LONG-NAME=\"Active\"><TYPE><DATATYPE-DEFINITION-BOOLEAN-REF>dt-boolean</DATATYPE-DEFINITION-BOOLEAN-REF></TYPE></ATTRIBUTE-DEFINITION-BOOLEAN></test>");
    }

    @SuperBuilder
    @Getter
    private static class UnKnownAttributeDefinition extends AttributeDefinition {
        @Override
        public DatatypeDefinition getType() {
            return null;
        }
    }
}
