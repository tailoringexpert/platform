package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.Specification;
import eu.tailoringexpert.domain.SpecificationType;
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

import static org.assertj.core.api.Assertions.assertThat;

class SpecificationConsumerTest {

    XmlMapper xmlMapper;
    XMLOutputFactory xmlFactory;
    SpecificationConsumer consumer;

    @BeforeEach
    void setup() {
        this.xmlMapper = XmlMapper.builder().build();
        this.xmlFactory = this.xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory();
        this.consumer = new SpecificationConsumer();
    }

    @Test
    void accept_Specification_SpecificationConsumed() throws XMLStreamException, IOException {
        // arrange
        Specification specification = Specification.builder()
            .identifier("spec-1")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .longName("Normative Statement")
            .type(SpecificationType.builder()
                .build())
            .build();


        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(specification, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw.toString())
            .isNotEmpty()
            .isEqualTo("<test><SPECIFICATION IDENTIFIER=\"spec-1\" LONG-NAME=\"Normative Statement\" LAST-CHANGE=\"2026-01-01T00:00\"/></test>");
    }

}
