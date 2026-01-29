package eu.tailoringexpert.serializer;

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
class DatatypeDefinitionEnumerationSerializerTest {
    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new DatatypeDefinitionEnumerationSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void serialize_DatatypeDefinitionEnumeration_ElementSerialized() {
        // arrange
        DatatypeDefinitionEnumeration datatypeDefinition = DatatypeDefinitionEnumeration.builder()
            .identifier("dt-kind")
            .longName("Kind")
            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
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
                        .build()
                )
            )
            .build();

        // act
        String actual = xmlMapper.writeValueAsString(datatypeDefinition);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<DATATYPE-DEFINITION-ENUMERATION IDENTIFIER=\"dt-kind\" LONG-NAME=\"Kind\" LAST-CHANGE=\"2026-01-01T00:00\"><SPECIFIED-VALUES><ENUM-VALUE IDENTIFIER=\"v-req\" LONG-NAME=\"Requirement\"><PROPERTIES><EMBEDDED-VALUE KEY=\"0\" OTHER-CONTENT=\"Requirement\"/></PROPERTIES></ENUM-VALUE><ENUM-VALUE IDENTIFIER=\"v-moc\" LONG-NAME=\"Means of Compliance\"><PROPERTIES><EMBEDDED-VALUE KEY=\"1\" OTHER-CONTENT=\"Means of Compliance\"/></PROPERTIES></ENUM-VALUE></SPECIFIED-VALUES></DATATYPE-DEFINITION-ENUMERATION>");
    }


}
