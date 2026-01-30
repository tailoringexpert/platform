package eu.tailoringexpert.domain;

import eu.tailoringexpert.serializer.AttributeDefinitionBooleanSerializer;
import eu.tailoringexpert.serializer.AttributeDefinitionEnumerationSerializer;
import eu.tailoringexpert.serializer.AttributeDefinitionStringSerializer;
import eu.tailoringexpert.serializer.AttributeValueBooleanSerializer;
import eu.tailoringexpert.serializer.AttributeValueEnumerationSerializer;
import eu.tailoringexpert.serializer.AttributeValueStringSerializer;
import eu.tailoringexpert.serializer.DatatypeDefinitionBooleanSerializer;
import eu.tailoringexpert.serializer.DatatypeDefinitionEnumerationSerializer;
import eu.tailoringexpert.serializer.DatatypeDefinitionStringSerializer;
import eu.tailoringexpert.serializer.ReqIFHeaderSerializer;
import eu.tailoringexpert.serializer.SpecObjectSerializer;
import eu.tailoringexpert.serializer.SpecObjectTypeSerializer;
import eu.tailoringexpert.serializer.SpecificationSerializer;
import eu.tailoringexpert.serializer.SpecificationTypeSerializer;
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
class ReqIFTest {

    ObjectMapper objectMapper;

    @BeforeEach
    void setup() {

        SimpleModule reqIFModule = new SimpleModule();
        reqIFModule.addSerializer(ReqIFHeader.class, new ReqIFHeaderSerializer());
        reqIFModule.addSerializer(DatatypeDefinitionString.class, new DatatypeDefinitionStringSerializer());
        reqIFModule.addSerializer(DatatypeDefinitionBoolean.class, new DatatypeDefinitionBooleanSerializer());
        reqIFModule.addSerializer(DatatypeDefinitionEnumeration.class, new DatatypeDefinitionEnumerationSerializer());
        reqIFModule.addSerializer(AttributeDefinitionBoolean.class, new AttributeDefinitionBooleanSerializer());
        reqIFModule.addSerializer(AttributeDefinitionString.class, new AttributeDefinitionStringSerializer());
        reqIFModule.addSerializer(AttributeValueBoolean.class, new AttributeValueBooleanSerializer());
        reqIFModule.addSerializer(AttributeDefinitionEnumeration.class, new AttributeDefinitionEnumerationSerializer());
        reqIFModule.addSerializer(AttributeValueEnumeration.class, new AttributeValueEnumerationSerializer());
        reqIFModule.addSerializer(AttributeValueString.class, new AttributeValueStringSerializer());

        reqIFModule.addSerializer(SpecObject.class, new SpecObjectSerializer());

        reqIFModule.addSerializer(SpecificationType.class, new SpecificationTypeSerializer());
        reqIFModule.addSerializer(SpecObjectType.class, new SpecObjectTypeSerializer());

        reqIFModule.addSerializer(Specification.class, new SpecificationSerializer());


        this.objectMapper = XmlMapper.builder()
            .findAndAddModules()
            .enable(INDENT_OUTPUT)
            .addModule(reqIFModule)
            .build();
    }

    @Test
    void doit() {
        // arrange
        String identifier = UUID.randomUUID().toString();
        String sourceToolId = "TailoringExpert";
        String reqIFToolId = "TailoringExpert";
        String reqIFVersion = "1.2";

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
                            AttributeDefinitionEnumeration.builder()
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
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("a-document-code")
                                        .longName("Document Code")
                                        .build())
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
                                .build(),
                            AttributeValueString.builder()
                                .theValue("Requirement")
                                .definition(AttributeDefinitionString.builder()
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("a-puid")
                                        .longName("PUID")
                                        .build())
                                    .build())
                                .build(),
                            AttributeValueString.builder()
                                .theValue("ECSS-Q-ST-80_0720001")
                                .definition(AttributeDefinitionString.builder()
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("a-parent-puid")
                                        .longName("Parent PUID")
                                        .build())
                                    .build())
                                .build(),
                            AttributeValueString.builder()
                                .theValue("")
                                .definition(AttributeDefinitionString.builder()
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("a-chapter")
                                        .longName("Chapter")
                                        .build())
                                    .build())
                                .build(),
                            AttributeValueString.builder()
                                .theValue("Software product assurance programme implementation / Organization and responsibility / Organization")
                                .definition(AttributeDefinitionString.builder()
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("a-discipline")
                                        .longName("Discipline")
                                        .build())
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
                                .build(),
                            AttributeValueString.builder()
                                .theValue("The supplier shall ensure that an organizational structure is defined for software development, and that individuals have defined tasks and responsibilities.")
                                .definition(AttributeDefinitionString.builder()
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("a-description")
                                        .longName("Description")
                                        .build())
                                    .build())
                                .build(),
                            AttributeValueString.builder()
                                .theValue("")
                                .definition(AttributeDefinitionString.builder()
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("a-notes")
                                        .longName("Notes")
                                        .build())
                                    .build())
                                .build()
                        ))


                        //.type(SpecObjectType.builder().build())
                        .build()
                ))
                .specifications(List.of(
                        Specification.builder()
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
                            .build()
                    )
                )
                .build()

            )
            .toolExtensions(List.of(
                ReqIFToolExtension.builder()
                    .build()
            ))
            .build();

//                <SPECIFICATION IDENTIFIER="spec-1" LAST-CHANGE="2026-01-19T13:33:00.568643071Z" LONG-NAME="Normative Statements">
//            <TYPE>
//            <SPECIFICATION-TYPE-REF>st-spec</SPECIFICATION-TYPE-REF>
//            </TYPE>
//            <CHILDREN>
//            <SPEC-HIERARCHY IDENTIFIER="sh-1" LAST-CHANGE="2026-01-19T13:33:00.568643071Z">
//            <OBJECT>
//            <SPEC-OBJECT-REF>so-1</SPEC-OBJECT-REF>
//            </OBJECT>
//            </SPEC-HIERARCHY>
//            </CHILDREN>
//            </SPECIFICATION>

        // act
        String actual = objectMapper.writeValueAsString(reqIF);

        // assert
        log.debug(actual);
    }
}
