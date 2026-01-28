package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.DatatypeDefinition;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionEnumeration;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.EmbeddedValue;
import eu.tailoringexpert.domain.EnumValue;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DatatypeDefinitionsConsumerTest {
    XmlMapper xmlMapper;
    XMLOutputFactory xmlFactory;
    DatatypeDefinitionsConsumer consumer;

    @BeforeEach
    void setup() {
        this.xmlMapper = XmlMapper.builder().build();
        this.xmlFactory = this.xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory();
        this.consumer = new DatatypeDefinitionsConsumer();
    }

    @Test
    void accept_DatatypeDefinitionStringOnly_ListConsumed() throws XMLStreamException, IOException {
        // arrange
        List<DatatypeDefinition> specTypes = List.of(
            DatatypeDefinitionString.builder()
                .identifier("dt-string")
                .maxLength(1000)
                .longName("String")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build()
        );

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(specTypes, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw)
            .hasToString("<test><DATATYPES><DATATYPE-DEFINITION-STRING IDENTIFIER=\"dt-string\" LONG-NAME=\"String\" LAST-CHANGE=\"2026-01-01T00:00\"/></DATATYPES></test>");
    }

    @Test
    void accept_DatatypeDefinitionBooleanOnly_ListConsumed() throws XMLStreamException, IOException {
        // arrange
        List<DatatypeDefinition> specTypes = List.of(
            DatatypeDefinitionBoolean.builder()
                .identifier("dt-boolean")
                .longName("Boolean")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build()
        );

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(specTypes, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw)
            .hasToString("<test><DATATYPES><DATATYPE-DEFINITION-BOOLEAN IDENTIFIER=\"dt-boolean\" LONG-NAME=\"Boolean\" LAST-CHANGE=\"2026-01-01T00:00\"/></DATATYPES></test>");
    }

    @Test
    void accept_DatatypeDefinitionEnumerationOnly_ListConsumed() throws XMLStreamException, IOException {
        // arrange
        List<DatatypeDefinition> specTypes = List.of(
            DatatypeDefinitionEnumeration.builder()
                .identifier("dt-kind")
                .longName("Kind")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .specifiedValues(List.of(
                        EnumValue.builder()
                            .identifier("v-req")
                            .longName("Requirement")
                            .properties(EmbeddedValue.builder()
                                .key(0)
                                .otherContent("Requirement")
                                .build())
                            .build(),
                        EnumValue.builder()
                            .identifier("v-moc")
                            .longName("Means of Compliance")
                            .properties(EmbeddedValue.builder()
                                .key(1)
                                .otherContent("Means of Compliance")
                                .build())
                            .build()
                    )
                )
                .build()
        );

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(specTypes, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw)
            .hasToString("<test><DATATYPES><DATATYPE-DEFINITION-ENUMERATION IDENTIFIER=\"dt-kind\" LONG-NAME=\"Kind\" LAST-CHANGE=\"2026-01-01T00:00\"><SPECIFIED-VALUES><ENUM-VALUE IDENTIFIER=\"v-req\" LONG-NAME=\"Requirement\"><PROPERTIES><EMBEDDED-VALUE KEY=\"0\" OTHER-CONTENT=\"Requirement\"/></PROPERTIES></ENUM-VALUE><ENUM-VALUE IDENTIFIER=\"v-moc\" LONG-NAME=\"Means of Compliance\"><PROPERTIES><EMBEDDED-VALUE KEY=\"1\" OTHER-CONTENT=\"Means of Compliance\"/></PROPERTIES></ENUM-VALUE></SPECIFIED-VALUES></DATATYPE-DEFINITION-ENUMERATION></DATATYPES></test>");
    }
}
