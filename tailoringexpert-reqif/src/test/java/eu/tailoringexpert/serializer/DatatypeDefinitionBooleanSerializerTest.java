package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class DatatypeDefinitionBooleanSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new DatatypeDefinitionBooleanSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_DatatypeDefinitionBoolean_DatatypedefintionStringConsumed() {
        // arrange
        DatatypeDefinitionBoolean datatypeDefinition = DatatypeDefinitionBoolean.builder()
            .identifier("dt-boolean")
            .longName("Boolean")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .build();

        // act
        String actual = xmlMapper.writeValueAsString(datatypeDefinition);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<DATATYPE-DEFINITION-BOOLEAN IDENTIFIER=\"dt-boolean\" LONG-NAME=\"Boolean\" LAST-CHANGE=\"2026-01-01T00:00\"/>");
    }


}
