package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.*;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class SpecRelationSerializerTest {

    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new SpecRelationSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void serialize_Specification_SpecificationConsumed() {
        // arrange
        SpecRelation specRelation = SpecRelation.builder()
            .identifier("sr-1")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .type(SpecRelationType.builder()
                .identifier("sr-reference")
                .longName("Relation to reference")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build())
            .source(SpecObject.builder()
                .identifier("so-reference-01")
                .build())
            .target(SpecObject.builder()
                .identifier("so-reference-02")
                .build())
            .build();

        // act
        String actual = xmlMapper.writeValueAsString(specRelation);

        // assert
        log.debug(actual);
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<SPEC-RELATION IDENTIFIER=\"sr-1\" LAST-CHANGE=\"2026-01-01T00:00\"><TYPE><SPECIFICATION-TYPE-REF>sr-reference</SPECIFICATION-TYPE-REF></TYPE><SOURCE><SPEC-OBJECT-REF>so-reference-01</SPEC-OBJECT-REF></SOURCE><TARGET><SPEC-OBJECT-REF>so-reference-02</SPEC-OBJECT-REF></TARGET></SPEC-RELATION>");
    }

}
