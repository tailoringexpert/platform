package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class AttributeDefinitionBooleanSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new AttributeDefinitionBooleanSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_DatatypeDefinitionString_DatatypedefintionStringConsumed() {
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

        // act
        String actual = xmlMapper.writeValueAsString(attributeDefinition);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<ATTRIBUTE-DEFINITION-BOOLEAN IDENTIFIER=\"a-active\" LONG-NAME=\"Active\"><TYPE><DATATYPE-DEFINITION-BOOLEAN-REF>dt-boolean</DATATYPE-DEFINITION-BOOLEAN-REF></TYPE></ATTRIBUTE-DEFINITION-BOOLEAN>");
    }
}
