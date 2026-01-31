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
import eu.tailoringexpert.domain.SpecHierarchy;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecObjectType;
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
class SpecObjectSerializerTest {

    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new SpecObjectSerializer());
        reqIfModule.addSerializer(new AttributeValueStringSerializer());
        reqIfModule.addSerializer(new AttributeValueBooleanSerializer());
        reqIfModule.addSerializer(new AttributeValueEnumerationSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void serialize_Specification_SpecificationConsumed() {
        // arrange
        SpecObject specObject = SpecObject.builder()
            .identifier("so-1")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .type(SpecObjectType.builder()
                .identifier("st-normative")
                .longName("Normative Statement")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .specAttributes(List.of(
                    AttributeDefinitionString.builder()
                        .identifier("a-document-code")
                        .build()
                ))
                .build())
            .values(List.of(
                AttributeValueString.builder()
                    .theValue("Q-80")
                    .definition(AttributeDefinitionString.builder()
                        .type(DatatypeDefinitionString.builder()
                            .identifier("a-document-code")
                            .longName("Document Code")
                            .build())
                        .build())
                    .build(),

                AttributeValueEnumeration.builder()
                    .values(List.of(
                        EnumValue.builder()
                            .identifier("v-ecss-q-st-80d software product assurance")
                            .build()
                    ))
                    .definition(AttributeDefinitionEnumeration.builder()
                        .identifier("a-type")
                        .build())
                    .build(),
                AttributeValueBoolean.builder()
                    .theValue(true)
                    .definition(AttributeDefinitionBoolean.builder()
                        .type(DatatypeDefinitionBoolean.builder()
                            .identifier("a-active")
                            .longName("Active")
                            .build())
                        .build())
                    .build()
            ))
            .build();


        // act
        String actual = xmlMapper.writeValueAsString(specObject);

        // assert
        log.debug(actual);
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<SPEC-OBJECT IDENTIFIER=\"so-1\" LAST-CHANGE=\"2026-01-01T00:00\"><TYPE><SPEC-OBJECT-TYPE-REF>st-normative</SPEC-OBJECT-TYPE-REF></TYPE><VALUES><ATTRIBUTE-VALUE-STRING THE-VALUE=\"Q-80\"><DEFINITION><ATTRIBUTE-DEFINITION-STRING-REF>a-document-code</ATTRIBUTE-DEFINITION-STRING-REF></DEFINITION></ATTRIBUTE-VALUE-STRING><ATTRIBUTE-VALUE-ENUMERATION><VALUES><ENUM-VALUE-REF>v-ecss-q-st-80d software product assurance</ENUM-VALUE-REF></VALUES><DEFINITION><ATTRIBUTE-DEFINITION-ENUMERATION-REF>a-type</ATTRIBUTE-DEFINITION-ENUMERATION-REF></DEFINITION></ATTRIBUTE-VALUE-ENUMERATION><ATTRIBUTE-VALUE-BOOLEAN THE-VALUE=\"true\"><DEFINITION><ATTRIBUTE-DEFINITION-BOOLEAN-REF>a-active</ATTRIBUTE-DEFINITION-BOOLEAN-REF></DEFINITION></ATTRIBUTE-VALUE-BOOLEAN></VALUES></SPEC-OBJECT>");
    }

    @Test
    void accept_Specification_SpecificationConsumed() {
        // arrange
        SpecObject specObject = SpecObject.builder()
            .identifier("so-1")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
            .type(SpecObjectType.builder()
                .identifier("st-normative")
                .longName("Normative Statement")
                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                .specAttributes(List.of(
                    AttributeDefinitionString.builder()
                        .identifier("a-document-code")
                        .build()
                ))
                .build())
            .values(List.of(
                AttributeValueString.builder()
                    .theValue("Q-80")
                    .definition(AttributeDefinitionString.builder()
                        .type(DatatypeDefinitionString.builder()
                            .identifier("a-document-code")
                            .longName("Document Code")
                            .build())
                        .build())
                    .build(),

                AttributeValueEnumeration.builder()
                    .values(List.of(
                        EnumValue.builder()
                            .identifier("v-ecss-q-st-80d software product assurance")
                            .build()
                    ))
                    .definition(AttributeDefinitionEnumeration.builder()
                        .identifier("a-type")
                        .build())
                    .build(),
                AttributeValueBoolean.builder()
                    .theValue(true)
                    .definition(AttributeDefinitionBoolean.builder()
                        .type(DatatypeDefinitionBoolean.builder()
                            .identifier("a-active")
                            .longName("Active")
                            .build())
                        .build())
                    .build()
            ))
            .build();


        // act
        String actual = xmlMapper.writeValueAsString(specObject);

        // assert
        log.debug(actual);
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<SPEC-OBJECT IDENTIFIER=\"so-1\" LAST-CHANGE=\"2026-01-01T00:00\"><TYPE><SPEC-OBJECT-TYPE-REF>st-normative</SPEC-OBJECT-TYPE-REF></TYPE><VALUES><ATTRIBUTE-VALUE-STRING THE-VALUE=\"Q-80\"><DEFINITION><ATTRIBUTE-DEFINITION-STRING-REF>a-document-code</ATTRIBUTE-DEFINITION-STRING-REF></DEFINITION></ATTRIBUTE-VALUE-STRING><ATTRIBUTE-VALUE-ENUMERATION><VALUES><ENUM-VALUE-REF>v-ecss-q-st-80d software product assurance</ENUM-VALUE-REF></VALUES><DEFINITION><ATTRIBUTE-DEFINITION-ENUMERATION-REF>a-type</ATTRIBUTE-DEFINITION-ENUMERATION-REF></DEFINITION></ATTRIBUTE-VALUE-ENUMERATION><ATTRIBUTE-VALUE-BOOLEAN THE-VALUE=\"true\"><DEFINITION><ATTRIBUTE-DEFINITION-BOOLEAN-REF>a-active</ATTRIBUTE-DEFINITION-BOOLEAN-REF></DEFINITION></ATTRIBUTE-VALUE-BOOLEAN></VALUES></SPEC-OBJECT>");
    }

}
