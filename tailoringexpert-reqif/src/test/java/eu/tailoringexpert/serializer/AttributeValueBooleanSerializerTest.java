package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class AttributeValueBooleanSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new AttributeValueBooleanSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_DatatypeDefinitionString_DatatypedefintionStringConsumed()  {
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

        // act
        String actual = xmlMapper.writeValueAsString(attributeValue);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<ATTRIBUTE-VALUE-BOOLEAN THE-VALUE=\"true\"><DEFINITION><ATTRIBUTE-DEFINITION-BOOLEAN-REF>a-active</ATTRIBUTE-DEFINITION-BOOLEAN-REF></DEFINITION></ATTRIBUTE-VALUE-BOOLEAN>");
    }


}
