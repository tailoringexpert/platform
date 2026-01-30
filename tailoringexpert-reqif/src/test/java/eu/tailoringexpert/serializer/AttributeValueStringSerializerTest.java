package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class AttributeValueStringSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new AttributeValueStringSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_DatatypeDefinitionString_DatatypedefintionStringConsumed()  {
        // arrange
        AttributeValueString attributeValue = AttributeValueString.builder()
            .theValue("Requirement")
            .definition(AttributeDefinitionString.builder()
                .type(DatatypeDefinitionString.builder()
                    .identifier("a-puid")
                    .longName("PUID")
                    .build())
                .build())
            .build();

        // act
        String actual = xmlMapper.writeValueAsString(attributeValue);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<ATTRIBUTE-VALUE-STRING THE-VALUE=\"Requirement\"><DEFINITION><ATTRIBUTE-DEFINITION-STRING-REF>a-puid</ATTRIBUTE-DEFINITION-STRING-REF></DEFINITION></ATTRIBUTE-VALUE-STRING>");
    }


}
