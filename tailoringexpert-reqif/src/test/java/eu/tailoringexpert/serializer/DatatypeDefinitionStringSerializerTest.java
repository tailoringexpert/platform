package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.DatatypeDefinitionString;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class DatatypeDefinitionStringSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new DatatypeDefinitionStringSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_DatatypeDefinitionString_DatatypedefintionStringConsumed() {
        // arrange
        DatatypeDefinitionString datatypeDefinition = DatatypeDefinitionString.builder()
            .identifier("dt-string")
            .maxLength(1000)
            .longName("String")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .build();

        // act
        String actual = xmlMapper.writeValueAsString(datatypeDefinition);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<DATATYPE-DEFINITION-STRING IDENTIFIER=\"dt-string\" LONG-NAME=\"String\" LAST-CHANGE=\"2026-01-01T00:00\" MAX-LENGTH=\"1000\"/>");
    }


}
