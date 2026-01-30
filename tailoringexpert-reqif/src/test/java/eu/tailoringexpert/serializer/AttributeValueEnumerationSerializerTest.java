package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.EnumValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class AttributeValueEnumerationSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new AttributeValueEnumerationSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_DatatypeDefinitionString_DatatypedefintionStringConsumed() {
        // arrange
        AttributeValueEnumeration attributeValue = AttributeValueEnumeration.builder()
            .values(List.of(
                EnumValue.builder()
                    .identifier("v-ecss-q-st-80d software product assurance")
                    .build()
            ))
            .definition(AttributeDefinitionEnumeration.builder()
                .identifier("a-type")
                .build())
            .build();

        // act
        String actual = xmlMapper.writeValueAsString(attributeValue);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<ATTRIBUTE-VALUE-ENUMERATION><VALUES><ENUM-VALUE-REF>v-ecss-q-st-80d software product assurance</ENUM-VALUE-REF></VALUES><DEFINITION><ATTRIBUTE-DEFINITION-ENUMERATION-REF>a-type</ATTRIBUTE-DEFINITION-ENUMERATION-REF></DEFINITION></ATTRIBUTE-VALUE-ENUMERATION>");
    }


}
