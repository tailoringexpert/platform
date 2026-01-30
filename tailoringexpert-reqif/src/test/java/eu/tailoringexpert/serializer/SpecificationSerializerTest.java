package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.SpecHierarchy;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.Specification;
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
class SpecificationSerializerTest {

    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new SpecificationSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_Specification_SpecificationConsumed() {
        // arrange
        Specification specification = Specification.builder()
            .identifier("spec-1")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .longName("Normative Statement")
            .type(SpecificationType.builder()
                .identifier("st-spec")
                .build())
            .children(List.of(
                    SpecHierarchy.builder()
                        .identifier("sh-1")
                        .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                        .object(SpecObject.builder()
                            .identifier("so-1")
                            .build())
                        .build()
                )
            )
            .build();


        // act
        String actual = xmlMapper.writeValueAsString(specification);

        // assert
        log.debug(actual);
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<SPECIFICATION IDENTIFIER=\"spec-1\" LONG-NAME=\"Normative Statement\" LAST-CHANGE=\"2026-01-01T00:00\"><TYPE><SPECIFICATION-TYPE-REF>st-spec</SPECIFICATION-TYPE-REF></TYPE><CHILDREN><SPEC-HIERARCHY IDENTIFIER=\"sh-1\" LAST-CHANGE=\"2026-01-01T00:00\"><OBJECT><SPEC-OBJECT-REF>so-1</SPEC-OBJECT-REF></OBJECT></SPEC-HIERARCHY></CHILDREN></SPECIFICATION>");
    }

}
