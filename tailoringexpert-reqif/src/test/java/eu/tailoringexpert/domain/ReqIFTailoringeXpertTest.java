package eu.tailoringexpert.domain;

import eu.tailoringexpert.serializer.*;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;

import static java.util.List.of;
import static tools.jackson.databind.SerializationFeature.INDENT_OUTPUT;

@Log4j2
class ReqIFTailoringeXpertTest {

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

        reqIFModule.addSerializer(SpecRelation.class, new SpecRelationSerializer());

        this.objectMapper = XmlMapper.builder()
            .findAndAddModules()
            .enable(INDENT_OUTPUT)
            .addModule(reqIFModule)
            .build();
    }

    @Test
    void serialize() {
        //arrange
        ReqIF txReqIF = ReqIF.builder()
            .theHeader(ReqIFHeader.builder()
                .creationTimestamp(LocalDateTime.of(2026, 1, 1, 0, 0))
                .identifier("tx example")
                .repositoryId("repositoryId")
                .reqIFToolId("TailoringeXpert")
                .sourceToolId("TailoringeXpert")
                .reqIFVersion("1.2")
                .title("DLR AR-SU Tailoringcatalogue")
                .build())
            .coreContent(ReqIFContent.builder()
                .datatypes(of(
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
                            .specifiedValues(of(
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
                .specTypes(of(
                    SpecObjectType.builder()
                        .identifier("type_chapter")
                        .longName("Chapter")
                        .specAttributes(of(
                            AttributeDefinitionString.builder()
                                .identifier("attr_name")
                                .longName("Name of the chapter")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(200)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build()
                        ))
                        .build(),

                    SpecObjectType.builder()
                        .identifier("type_reference")
                        .longName("Reference to original requirement")
                        .specAttributes(of(
                            AttributeDefinitionString.builder()
                                .identifier("attr_text")
                                .longName("text of reference document")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(200)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("attr_issue")
                                .longName("Issue of the reqirement")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(200)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build(),
                            AttributeDefinitionBoolean.builder()
                                .identifier("attr_changed")
                                .longName("State if  requirerment is modified")
                                .type(DatatypeDefinitionBoolean.builder()
                                    .identifier("dt-boolean")
                                    .longName("Boolean")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("attr_releasedate")
                                .longName("Date of released document reference")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(100)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("attr_logoname")
                                .longName("Name of the logo")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(200)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("attr_logourl")
                                .longName("Url of the logo")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(1000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build()
                        ))
                        .build(),

                    SpecObjectType.builder()
                        .identifier("type_requirement")
                        .longName("Requirmenet")
                        .specAttributes(of(
                            AttributeDefinitionString.builder()
                                .identifier("attr_number")
                                .longName("Number of the reqirerment")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(200)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build(),
                            AttributeDefinitionString.builder()
                                .identifier("attr_text")
                                .longName("Requirementtext")
                                .type(DatatypeDefinitionString.builder()
                                    .maxLength(10000)
                                    .identifier("dt-string")
                                    .longName("String")
                                    .lastChange(LocalDateTime.now())
                                    .build())
                                .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build()
                        ))
                        .build()
                ))
                .specObjects(of(
                    SpecObject.builder()
                        .identifier("chapter_1")
                        .type(SpecObjectType.builder()
                            .identifier("type_chapter")
                            .longName("Chapter")
                            .specAttributes(of(
                                AttributeDefinitionString.builder()
                                    .identifier("attr_number")
                                    .longName("Number of the chapter")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(20)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build(),
                                AttributeDefinitionString.builder()
                                    .identifier("attr_name")
                                    .longName("Name of the chapter")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(200)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build()
                            ))
                            .build())
                        .values(of(
                            AttributeValueString.builder()
                                .theValue("1. General")
                                .definition(AttributeDefinitionString.builder()
                                    .type(DatatypeDefinitionString.builder()
                                        .identifier("attr_title")
                                        .build())
                                    .build())
                                .build()
                        ))
                        .build(),
                    SpecObject.builder()
                        .identifier("sp-01-reference")
                        .type(SpecObjectType.builder()
                            .identifier("type_reference")
                            .longName("Reference to original requirement")
                            .specAttributes(of(
                                AttributeDefinitionString.builder()
                                    .identifier("attr_text")
                                    .longName("text of reference document")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(200)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build(),
                                AttributeDefinitionString.builder()
                                    .identifier("attr_issue")
                                    .longName("Issue of the reqirement")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(200)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build(),
                                AttributeDefinitionBoolean.builder()
                                    .identifier("attr_changed")
                                    .longName("State if  requirerment is modified")
                                    .type(DatatypeDefinitionBoolean.builder()
                                        .identifier("dt-boolean")
                                        .longName("Boolean")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build(),
                                AttributeDefinitionString.builder()
                                    .identifier("attr_releasedate")
                                    .longName("Date of released document reference")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(100)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build(),
                                AttributeDefinitionString.builder()
                                    .identifier("attr_logoname")
                                    .longName("Name of the logo")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(200)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build(),
                                AttributeDefinitionString.builder()
                                    .identifier("attr_logourl")
                                    .longName("Url of the logo")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(1000)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build()
                            ))
                            .build())
                        .values(of(

                        ))
                        .build(),
                    SpecObject.builder()
                        .identifier("sp-02-requirement")
                        .type(SpecObjectType.builder()
                            .identifier("type_requirement")
                            .longName("Requirmenet")
                            .specAttributes(of(
                                AttributeDefinitionString.builder()
                                    .identifier("attr_number")
                                    .longName("Number of the reqirerment")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(200)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build(),
                                AttributeDefinitionString.builder()
                                    .identifier("attr_text")
                                    .longName("Requirementtext")
                                    .type(DatatypeDefinitionString.builder()
                                        .maxLength(10000)
                                        .identifier("dt-string")
                                        .longName("String")
                                        .lastChange(LocalDateTime.now())
                                        .build())
                                    .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                                    .build()
                            ))
                            .build()
                        )
                        .values(of(

                        ))
                        .build()
                    ))
                .specRelations(of(
                    SpecRelation.builder()
                        .identifier("sr-1")
                        .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                        .type(SpecRelationType.builder()
                            .identifier("sr-reference")
                            .longName("Relation to reference")
                            .lastChange(LocalDateTime.of(2026, 1, 1, 0, 0))
                            .build())
                        .source(SpecObject.builder()
                            .identifier("sp-01-reference")
                            .build())
                        .target(SpecObject.builder()
                            .identifier("sp-02-requirment")
                            .build())
                        .build()
                ))
                .build()
            )
            .build();

        // act
        String actual = objectMapper.writeValueAsString(txReqIF);

        // assert
        log.debug(actual);
    }

//            </SPEC-OBJECT>
//
//            <!-- Dieses Objekt ist eine Anforderung -->
//            <SPEC-OBJECT IDENTIFIER="obj_2" LAST-CHANGE="2023-10-27T10:00:00Z">
//            <TYPE>
//            <SPEC-OBJECT-TYPE-REF>type_requirement</SPEC-OBJECT-TYPE-REF>
//            </TYPE>
//            <VALUES>
//            <ATTRIBUTE-VALUE-STRING THE-VALUE="Das System muss 100 bar Druck standhalten.">
//            <DEFINITION><ATTRIBUTE-DEFINITION-STRING-REF>attr_desc</ATTRIBUTE-DEFINITION-STRING-REF></DEFINITION>
//            </ATTRIBUTE-VALUE-STRING>
//            </VALUES>
//            </SPEC-OBJECT>
//            </SPEC-OBJECTS>
//    }
}
