package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.EnumValue;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecObjectType;
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

class SpecObjectConsumerTest {

    XmlMapper xmlMapper;
    XMLOutputFactory xmlFactory;
    SpecObjectConsumer consumer;

    @BeforeEach
    void setup() {
        this.xmlMapper = XmlMapper.builder().build();
        this.xmlFactory = this.xmlMapper
            .tokenStreamFactory()
            .getXMLOutputFactory();
        this.consumer = new SpecObjectConsumer();
    }

    @Test
    void accept_SpecObject_SpecObjectConsumed() throws XMLStreamException, IOException {
        // arrange
        SpecObject specObject = SpecObject.builder()
            .identifier("so-1")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .type(SpecObjectType.builder()
                .identifier("st-normative")
                .longName("Normative Statement")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .specAttributes(List.of(
                    AttributeDefinitionString.builder()
                        .identifier("a-document-code")
                        .build()
                ))
                .build())
            .values(List.of(
                AttributeValueString.builder()
                    .theValue("Q-80")
                    .definition(AttributeDefinitionString.builder()
                        .type(DatatypeDefinitionString.builder()
                            .identifier("a-document-code")
                            .longName("Document Code")
                            .build())
                        .build())
                    .build(),

                AttributeValueEnumeration.builder()
                    .values(List.of(
                        EnumValue.builder()
                            .identifier("v-ecss-q-st-80d software product assurance")
                            .build()
                    ))
                    .definition(AttributeDefinitionEnumeration.builder()
                        .identifier("a-type")
                        .build())
                    .build(),
                AttributeValueBoolean.builder()
                    .theValue(true)
                    .definition(AttributeDefinitionBoolean.builder()
                        .type(DatatypeDefinitionBoolean.builder()
                            .identifier("a-active")
                            .longName("Active")
                            .build())
                        .build())
                    .build()
            ))
            .build();

        StringWriter sw = new StringWriter();
        XMLStreamWriter xmlWriter = xmlFactory.createXMLStreamWriter(sw);
        ToXmlGenerator generator = xmlMapper.createGenerator(xmlWriter);

        // act
        generator.setNextName(new QName("test"));
        generator.writeStartObject();

        consumer.accept(specObject, generator);

        generator.writeEndObject();
        xmlWriter.close();

        // assert
        assertThat(sw.toString())
            .isNotEmpty()
            .isEqualTo("<test><SPEC-OBJECT IDENTIFIER=\"so-1\" LAST-CHANGE=\"2026-01-01T00:00\"><TYPE><SPEC-OBJECT-TYPE-REF>st-normative</SPEC-OBJECT-TYPE-REF></TYPE><VALUES><ATTRIBUTE-VALUE-STRING THE-VALUE=\"Q-80\"><DEFINITION><ATTRIBUTE-DEFINITION-STRING-REF>a-document-code</ATTRIBUTE-DEFINITION-STRING-REF></DEFINITION></ATTRIBUTE-VALUE-STRING><ATTRIBUTE-VALUE-ENUMERATION><VALUES><ENUM-VALUE-REF>v-ecss-q-st-80d software product assurance</ENUM-VALUE-REF></VALUES><DEFINITION><ATTRIBUTE-DEFINITION-ENUMERATION-REF>a-type</ATTRIBUTE-DEFINITION-ENUMERATION-REF></DEFINITION></ATTRIBUTE-VALUE-ENUMERATION><ATTRIBUTE-VALUE-BOOLEAN THE-VALUE=\"true\"><DEFINITION><ATTRIBUTE-DEFINITION-BOOLEAN-REF>a-active</ATTRIBUTE-DEFINITION-BOOLEAN-REF></DEFINITION></ATTRIBUTE-VALUE-BOOLEAN></VALUES></SPEC-OBJECT></test>");
    }
}
