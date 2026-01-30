package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.Identifiable;
import eu.tailoringexpert.serializer.IdentifiableConsumer;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class IdentifiableConsumerTest {
    XmlMapper xmlMapper;
    IdentifiableConsumer consumer;

    @BeforeEach
    void setup() {
        this.xmlMapper = XmlMapper.builder().build();
        this.consumer = new IdentifiableConsumer();
    }

    @Test
    void accept_NoLastChange_StringWithoutLastChangeCreated() throws XMLStreamException, IOException {
        // arrange
        Identifiable identifiable = JUnitIdentifiable.builder()
            .identifier("identifibale-260127-01")
            .longName("An example identifiable")
            .desc("example description")
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

        consumer.accept(identifiable, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw)
            .hasToString("<test IDENTIFIER=\"identifibale-260127-01\" LONG-NAME=\"An example identifiable\"/>");
    }

    @Test
    void accept_AllIdentifiableProps_StringWithLastChangeCreated() throws XMLStreamException, IOException {
        // arrange
        Identifiable identifiable = JUnitIdentifiable.builder()
            .identifier("identifibale-260127-01")
            .longName("An example identifiable")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .desc("example description")
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

        consumer.accept(identifiable, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw)
            .hasToString("<test IDENTIFIER=\"identifibale-260127-01\" LONG-NAME=\"An example identifiable\" LAST-CHANGE=\"2026-01-01T00:00\"/>");
    }

    @SuperBuilder
    @Getter
    private static class JUnitIdentifiable extends Identifiable {
    }
}

