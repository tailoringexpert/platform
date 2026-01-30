package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.DatatypeDefinitionEnumeration;
import eu.tailoringexpert.domain.EmbeddedValue;
import eu.tailoringexpert.domain.EnumValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class AttributeDefinitionEnumerationSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new AttributeDefinitionEnumerationSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_DatatypeDefinitionString_DatatypedefintionStringConsumed() {
        // arrange
        AttributeDefinitionEnumeration attributeDefinition = AttributeDefinitionEnumeration.builder()
            .identifier("a-type")
            .longName("Type")
            .type(DatatypeDefinitionEnumeration.builder()
                .identifier("dt-kind")
                .longName("Kind")
                .lastChange(LocalDateTime.now())
                .specifiedValues(List.of(
                        EnumValue.builder()
                            .identifier("v-req")
                            .longName("Requirement")
                            .properties(EmbeddedValue.builder()
                                .key(0)
                                .otherContent("Requirement")
                                .build())
                            .build(),
                        EnumValue.builder()
                            .identifier("v-moc")
                            .longName("Means of Compliance")
                            .properties(EmbeddedValue.builder()
                                .key(1)
                                .otherContent("Means of Compliance")
                                .build())
                            .build(),
                        EnumValue.builder()
                            .identifier("v-tp")
                            .longName("Technical Procedure")
                            .properties(EmbeddedValue.builder()
                                .key(2)
                                .otherContent("Technical Procedure")
                                .build())
                            .build(),
                        EnumValue.builder()
                            .identifier("v-ts")
                            .longName("Technical Specification")
                            .properties(EmbeddedValue.builder()
                                .key(3)
                                .otherContent("Technical Specification")
                                .build())
                            .build()
                    )
                )
                .build())
            .build();

        // act
        String actual = xmlMapper.writeValueAsString(attributeDefinition);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<ATTRIBUTE-DEFINITION-ENUMERATION IDENTIFIER=\"a-type\" LONG-NAME=\"Type\"><TYPE><DATATYPE-DEFINITION-ENUMERATION-REF>dt-kind</DATATYPE-DEFINITION-ENUMERATION-REF></TYPE></ATTRIBUTE-DEFINITION-ENUMERATION>");
    }


}
