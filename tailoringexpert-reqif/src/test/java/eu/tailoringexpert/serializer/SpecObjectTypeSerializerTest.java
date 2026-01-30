package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.EnumValue;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecObjectType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class SpecObjectTypeSerializerTest {

    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new SpecObjectTypeSerializer());
        reqIfModule.addSerializer(new AttributeValueStringSerializer());
        reqIfModule.addSerializer(new AttributeDefinitionStringSerializer());
        reqIfModule.addSerializer(new AttributeValueBooleanSerializer());
        reqIfModule.addSerializer(new AttributeDefinitionBooleanSerializer());
        reqIfModule.addSerializer(new AttributeValueEnumerationSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_Specification_SpecificationConsumed() {
        // arrange
        SpecObjectType specType = SpecObjectType.builder()
            .identifier("st-normative")
            .longName("Normative Statement")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .specAttributes(List.of(
                    AttributeDefinitionString.builder()
                        .identifier("a-document-code")
                        .longName("Document Code")
                        .type(DatatypeDefinitionString.builder()
                            .maxLength(10000)
                            .identifier("dt-string")
                            .longName("String")
                            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                            .build())
                        .build(),
                    AttributeDefinitionBoolean.builder()
                        .identifier("a-active")
                        .longName("Active")
                        .type(DatatypeDefinitionBoolean.builder()
                            .identifier("dt-boolean")
                            .longName("Boolean")
                            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                            .build())
                        .build()
                )
            )
            .build();


        // act
        String actual = xmlMapper.writeValueAsString(specType);

        // assert
        log.debug(actual);
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<SPEC-OBJECT-TYPE IDENTIFIER=\"st-normative\" LONG-NAME=\"Normative Statement\" LAST-CHANGE=\"2026-01-01T00:00\"><SPEC-ATTRIBUTES><ATTRIBUTE-DEFINITION-STRING IDENTIFIER=\"a-document-code\" LONG-NAME=\"Document Code\"><TYPE><DATATYPE-DEFINITION-STRING-REF>dt-string</DATATYPE-DEFINITION-STRING-REF></TYPE></ATTRIBUTE-DEFINITION-STRING><ATTRIBUTE-DEFINITION-BOOLEAN IDENTIFIER=\"a-active\" LONG-NAME=\"Active\"><TYPE><DATATYPE-DEFINITION-BOOLEAN-REF>dt-boolean</DATATYPE-DEFINITION-BOOLEAN-REF></TYPE></ATTRIBUTE-DEFINITION-BOOLEAN></SPEC-ATTRIBUTES></SPEC-OBJECT-TYPE>");
    }

}
