package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecType;
import eu.tailoringexpert.domain.SpecificationType;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
class SpecTypesConsumerTest {

    XmlMapper xmlMapper;
    XMLOutputFactory xmlFactory;
    SpecTypesConsumer consumer;

    @BeforeEach
    void setup() {
        this.xmlMapper = XmlMapper.builder().build();
        this.xmlFactory = this.xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory();
        this.consumer = new SpecTypesConsumer();
    }

    @Test
    void accept_SpecObjectTypesOnly_SpecObjectConsumed() throws XMLStreamException, IOException {
        // arrange
        List<SpecType> specTypes = List.of(
            SpecObjectType.builder()
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
        log.debug(sw.toString());
        assertThat(sw)
            .hasToString("<test><SPEC-TYPES><SPEC-OBJECT-TYPE IDENTIFIER=\"st-normative\" LONG-NAME=\"Normative Statement\" LAST-CHANGE=\"2026-01-01T00:00\"><SPEC-ATTRIBUTES><ATTRIBUTE-DEFINITION-STRING IDENTIFIER=\"a-document-code\" LONG-NAME=\"Document Code\"><TYPE><DATATYPE-DEFINITION-STRING-REF>dt-string</DATATYPE-DEFINITION-STRING-REF></TYPE></ATTRIBUTE-DEFINITION-STRING><ATTRIBUTE-DEFINITION-BOOLEAN IDENTIFIER=\"a-active\" LONG-NAME=\"Active\"><TYPE><DATATYPE-DEFINITION-BOOLEAN-REF>dt-boolean</DATATYPE-DEFINITION-BOOLEAN-REF></TYPE></ATTRIBUTE-DEFINITION-BOOLEAN></SPEC-ATTRIBUTES></SPEC-OBJECT-TYPE></SPEC-TYPES></test>");
    }

    @Test
    void accept_SpecificationTypesOnly_CollectionConsumed() throws XMLStreamException, IOException {
        // arrange
        List<SpecType> specTypes = List.of(
            SpecificationType.builder()
                .identifier("st-spec")
                .longName("Specification Type")
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
        log.debug(sw.toString());
        assertThat(sw)
            .hasToString("<test><SPEC-TYPES><SPECIFICATION-TYPE IDENTIFIER=\"st-spec\" LONG-NAME=\"Specification Type\" LAST-CHANGE=\"2026-01-01T00:00\"/></SPEC-TYPES></test>");
    }
}
