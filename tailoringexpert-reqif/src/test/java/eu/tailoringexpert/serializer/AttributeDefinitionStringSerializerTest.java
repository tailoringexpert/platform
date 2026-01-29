package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class AttributeDefinitionStringSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new AttributeDefinitionStringSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_DatatypeDefinitionString_DatatypedefintionStringConsumed() throws XMLStreamException, IOException {
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

        // act
        String actual = xmlMapper.writeValueAsString(attributeDefinition);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<ATTRIBUTE-DEFINITION-STRING IDENTIFIER=\"a-document-code\" LONG-NAME=\"Document Code\"><TYPE><DATATYPE-DEFINITION-STRING-REF>dt-string</DATATYPE-DEFINITION-STRING-REF></TYPE></ATTRIBUTE-DEFINITION-STRING>");
    }


}
