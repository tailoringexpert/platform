package eu.tailoringexpert.domain;

import eu.tailoringexpert.reqif.ReqIFContentSerializer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static tools.jackson.databind.SerializationFeature.INDENT_OUTPUT;

@Log4j2
public class ReqIFTest {

    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {

        SimpleModule dynamicRootNameModule = new SimpleModule();
        dynamicRootNameModule.addSerializer(ReqIFContent.class, new ReqIFContentSerializer());
//        dynamicRootNameModule.addSerializer(SpecObjectType.class, new SpecObjectTypeSerializer());


        this.objectMapper = XmlMapper.builder()
            .findAndAddModules()
            .enable(INDENT_OUTPUT)
            .addModule(dynamicRootNameModule)
//            .propertyNamingStrategy(new PropertyNamingStrategies.UpperSnakeCaseStrategy())
//            .addTypeModifier(new DatatypeIDResolver())
            .build();
    }

    @Test
    void doit() {
        // arrange
        String identifier = UUID.randomUUID().toString();
        String sourceToolId = "TailoringExpert";
        String reqIFToolId = "TailoringExpert";
        String reqIFVersion = "1.2";

        List<DatatypeDefinitionString> datatypeDefinitions = List.of(
            DatatypeDefinitionString.builder()
                .maxLength(10000)
                .identifier("dt-string")
                .longName("String")
                .lastChange(LocalDateTime.now())
                .build()
        );
        ReqIF reqIF = ReqIF.builder()
            .theHeader(ReqIFHeader.builder()
                .creationTimestamp(LocalDateTime.now())
                .identifier(identifier)
                .repositoryId("repositoryId")
                .reqIFToolId(reqIFToolId)
                .reqIFVersion(reqIFVersion)
                .sourceToolId(sourceToolId)
                .title("DLR AR-SU Tailoringcatalogue")
                .build())
            .coreContent(ReqIFContent.builder()
                .datatypes(List.of(
                        DatatypeDefinitionString.builder()
                            .maxLength(10000)
                            .identifier("dt-string")
                            .longName("String")
                            .lastChange(LocalDateTime.now())
                            .build(),
                        DatatypeDefinitionEnumeration.builder()
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
                            .build(),
                        DatatypeDefinitionBoolean.builder()
                            .identifier("dt-boolean")
                            .longName("Boolean")
                            .lastChange(LocalDateTime.now())
                            .build()
                    )
                )
                .specTypes(List.of(
                    SpecObjectType.builder()
                        .identifier("st-normative")
                        .longName("Normative Statement")
                        .lastChange(LocalDateTime.now())
                        .specAttributes(List.of(
                            AttributeDefinitionString.builder()
                                .identifier("a-document-code")
                                .longName("Document Code")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("a-document-title")
                                .longName("Document title")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("a-type")
                                .longName("Type")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-kind")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("a-puid")
                                .longName("PUID")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("a-parent-puid")
                                .longName("Parent PUID")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("a-chapter")
                                .longName("Chapter")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("a-discipline")
                                .longName("Discipline")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionBoolean.builder()
                                .identifier("a-active")
                                .longName("Active")
                                .type(DatatypeDefinitionBoolean.builder()
                                    .identifier("dt-boolean")
                                    .longName("Boolean")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("a-discipline")
                                .longName("Discipline")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(), AttributeDefinitionString.builder()
                                .identifier("a-description")
                                .longName("Description")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("a-notes")
                                .longName("Notes")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .build()
                        ))
                        .build(),
                    SpecificationType.builder()
                        .identifier("st-spec")
                        .longName("Specification Type")
                        .lastChange(LocalDateTime.now())
                        .build()
                ))
                .specObjects(List.of(
                    SpecObject.builder()
                        .identifier("so-1")
                        .lastChange(LocalDateTime.now())
                        .type(SpecObjectType.builder()
                            .identifier("st-normative")
                            .longName("Normative Statement")
                            .lastChange(LocalDateTime.now())
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
                            AttributeValueString.builder()
                                .theValue("ECSS-Q-ST-80D Software product assurance")
                                .definition(AttributeDefinitionString.builder()
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("a-document-title")
                                        .longName("Document title")
                                        .build())
                                    .build())
                                .build(),
                            AttributeValueEnumeration.builder()
                                .values(List.of(
                                    EnumValue.builder()
                                        .identifier("v-ecss-q-st-80d software product assurance")
                                        .build()
                                ))
                                .build()
                        ))

//                        <ATTRIBUTE-VALUE-ENUMERATION>
//                        <VALUES>
//                        <ENUM-VALUE-REF>v-ecss-q-st-80d software product assurance</ENUM-VALUE-REF>
//                        </VALUES>
//                        <DEFINITION>
//                        <ATTRIBUTE-DEFINITION-ENUMERATION-REF>a-type</ATTRIBUTE-DEFINITION-ENUMERATION-REF>
//                        </DEFINITION>
//                        </ATTRIBUTE-VALUE-ENUMERATION>

                        //.type(SpecObjectType.builder().build())
                        .build()
                ))
                .build())
            .toolExtensions(List.of(
                ReqIFToolExtension.builder()
                    .build()
            ))
            .build();

        // act
        String actual = objectMapper.writeValueAsString(reqIF);

        // assert
        log.debug(actual);
    }
}
