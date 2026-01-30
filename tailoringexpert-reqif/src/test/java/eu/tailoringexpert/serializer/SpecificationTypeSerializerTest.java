package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecificationType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class SpecificationTypeSerializerTest {

    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new SpecificationTypeSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_Specification_SpecificationConsumed() {
        // arrange
        SpecificationType specType = SpecificationType.builder()
            .identifier("st-spec")
            .longName("Specification Type")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .build();


        // act
        String actual = xmlMapper.writeValueAsString(specType);

        // assert
        log.debug(actual);
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<SPECIFICATION-TYPE IDENTIFIER=\"st-spec\" LONG-NAME=\"Specification Type\" LAST-CHANGE=\"2026-01-01T00:00\"/>");
    }

}
