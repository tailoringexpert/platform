package eu.tailoringexpert.domain;

import eu.tailoringexpert.reqif.DynamicRootNameBeanSerializerModifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
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
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("eu.tailoringexpert.domain")
            .build();

        SimpleModule dynamicRootNameModule = new SimpleModule();
        dynamicRootNameModule.setSerializerModifier(new DynamicRootNameBeanSerializerModifier());


        this.objectMapper = XmlMapper.builder()
            .findAndAddModules()
            .enable(INDENT_OUTPUT)
            .addModule(dynamicRootNameModule)
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
                        .build())
                )
                .specTypes(List.of(
                    SpecType.builder()
                        .identifier("st-normative")
                        .longName("Normative Statement")
                        .lastChange(LocalDateTime.now())
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
