package eu.tailoringexpert.reqif;

import eu.tailoringexpert.domain.AttributeDefinitionBoolean;
import eu.tailoringexpert.domain.AttributeDefinitionEnumeration;
import eu.tailoringexpert.domain.AttributeDefinitionString;
import eu.tailoringexpert.domain.AttributeValueBoolean;
import eu.tailoringexpert.domain.AttributeValueEnumeration;
import eu.tailoringexpert.domain.AttributeValueString;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DatatypeDefinitionBoolean;
import eu.tailoringexpert.domain.DatatypeDefinitionEnumeration;
import eu.tailoringexpert.domain.DatatypeDefinitionString;
import eu.tailoringexpert.domain.ReqIFContent;
import eu.tailoringexpert.domain.ReqIFHeader;
import eu.tailoringexpert.domain.SpecObject;
import eu.tailoringexpert.domain.SpecObjectType;
import eu.tailoringexpert.domain.SpecRelation;
import eu.tailoringexpert.domain.Specification;
import eu.tailoringexpert.domain.SpecificationType;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
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
import eu.tailoringexpert.serializer.SpecRelationSerializer;
import eu.tailoringexpert.serializer.SpecificationSerializer;
import eu.tailoringexpert.serializer.SpecificationTypeSerializer;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.util.Map;

import static java.util.List.of;
import static tools.jackson.databind.SerializationFeature.INDENT_OUTPUT;

@Log4j2
public class ReqIFMapperTest {

    ReqIFMapper mapper;
    ObjectMapper objectMapper;;

    @BeforeEach
    void setup() {
        this.mapper = new ReqIFMapper();

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
    void doit() {
        // arrange
        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("1")
            .name("General")
            .position(1)
            .requirements(of(
                TailoringRequirement.builder()
                    .position("a")
                    .selected(true)
                    .text("Please note that for the project specifically tailored requirement catalogue the numbering of requirements and chapters might not always be continuous. There might be gaps in the continuous numeration. These gaps are intentionally and caused by requirements which are tailored out, as not applicable. It was decided to have the fixed numbering of requirements to allow for a general electronic handling of compliances in Systems as e.g. DOORs in projectindependent approaches")
                    .build()
            ))
            .chapters(of(
                Chapter.<TailoringRequirement>builder()
                    .number("1.1")
                    .name("Scope")
                    .position(1)
                    .requirements(of(
                        TailoringRequirement.builder()
                            .position("a")
                            .selected(true)
                            .text("This document defines the Product Assurance (PA) objectives, policy and rules for the establishment and implementation of a PA programme for DLR projects to ensure the required product quality for the project covering design, development, procurement, manufacturing, integration, test and operations of the contractual items.This document further provides a listing of potential supplier generated PA documents resulting from PA efforts identified in this document(Document Requirements List - DRL)")
                            .build()

                    ))
                    .chapters(of())
                    .build()
            ))
            .build();

        Tailoring tailoring = Tailoring.builder()
            .catalog(Catalog.<TailoringRequirement>builder()
                .toc(Chapter.<TailoringRequirement>builder()
                    .name("/")
                    .chapters(of(
                        chapter
                    ))
                    .build())
                .build())
            .build();


        // act
        ReqIFContent actual = mapper.doit(tailoring, Map.of());

        // assert
        log.debug(objectMapper.writeValueAsString(actual));
    }
}
