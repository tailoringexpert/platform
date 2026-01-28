package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;
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
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class SpecTypeConsumerTest {

    XmlMapper xmlMapper;
    XMLOutputFactory xmlFactory;
    SpecTypeConsumer consumer;

    @BeforeEach
    void setup() {
        this.xmlMapper = XmlMapper.builder().build();
        this.xmlFactory = this.xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory();
        this.consumer = new SpecTypeConsumer();
    }

    @Test
    void accept_UnknownType_NoSuchElementExceptionThrown() throws XMLStreamException, IOException {
        // arrange
        UnKnownSpecType specType = UnKnownSpecType.builder().build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory()
            .createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        Throwable actual = Assertions.catchThrowable(() -> consumer.accept(specType, generator));

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void accept_SpecObject_SpecObjectConsumed() throws XMLStreamException, IOException {
        // arrange
        SpecObjectType specType = SpecObjectType.builder()
            .identifier("st-normative")
            .longName("Normative Statement")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .specAttributes(List.of(
                    AttributeDefinitionString.builder()
                        .identifier("a-document-code")
                        .longName("Document Code")
                        .type(DatatypeDefinitionString.builder()
                            .maxLength(10000)
                            .identifier("dt-string")
                            .longName("String")
                            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                            .build())
                        .build(),
                    AttributeDefinitionBoolean.builder()
                        .identifier("a-active")
                        .longName("Active")
                        .type(DatatypeDefinitionBoolean.builder()
                            .identifier("dt-boolean")
                            .longName("Boolean")
                            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                            .build())
                        .build()
                )
            )
            .build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(specType, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        log.debug(sw.toString());
        assertThat(sw)
            .hasToString("<test><SPEC-OBJECT-TYPE IDENTIFIER=\"st-normative\" LONG-NAME=\"Normative Statement\" LAST-CHANGE=\"2026-01-01T00:00\"><SPEC-ATTRIBUTES><ATTRIBUTE-DEFINITION-STRING IDENTIFIER=\"a-document-code\" LONG-NAME=\"Document Code\"><TYPE><DATATYPE-DEFINITION-STRING-REF>dt-string</DATATYPE-DEFINITION-STRING-REF></TYPE></ATTRIBUTE-DEFINITION-STRING><ATTRIBUTE-DEFINITION-BOOLEAN IDENTIFIER=\"a-active\" LONG-NAME=\"Active\"><TYPE><DATATYPE-DEFINITION-BOOLEAN-REF>dt-boolean</DATATYPE-DEFINITION-BOOLEAN-REF></TYPE></ATTRIBUTE-DEFINITION-BOOLEAN></SPEC-ATTRIBUTES></SPEC-OBJECT-TYPE></test>");
    }

    @Test
    void accept_SpecificationType_SpecificationTypeConsumed() throws XMLStreamException, IOException {
        // arrange
        SpecificationType specType = SpecificationType.builder()
            .identifier("st-spec")
            .longName("Specification Type")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(specType, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        log.debug(sw.toString());
        assertThat(sw)
            .hasToString("<test><SPECIFICATION-TYPE IDENTIFIER=\"st-spec\" LONG-NAME=\"Specification Type\" LAST-CHANGE=\"2026-01-01T00:00\"/></test>");
    }

    @SuperBuilder
    @Getter
    private static class UnKnownSpecType extends SpecType {
    }
}
