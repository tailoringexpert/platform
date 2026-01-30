package eu.tailoringexpert.serializer;

import eu.tailoringexpert.domain.ReqIFHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.dataformat.xml.XmlMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ReqIFHeaderSerializerTest {

    XmlMapper xmlMapper;

    @BeforeEach
    void setup() {
        SimpleModule reqIfModule = new SimpleModule("ReqIFModule");
        reqIfModule.addSerializer(new ReqIFHeaderSerializer());

        this.xmlMapper = XmlMapper.builder()
            .addModule(reqIfModule)
            .build();
    }

    @Test
    void accept_Specification_SpecificationConsumed() {
        // arrange
        ReqIFHeader header = ReqIFHeader.builder()
            .creationTimestamp(LocalDateTime.now())
            .identifier("header-1")
            .repositoryId("repositoryId")
            .reqIFToolId("TailoringExpert")
            .reqIFVersion("1.2")
            .sourceToolId("TailoringExpert")
            .title("DLR AR-SU Tailoringcatalogue")
            .build();


        // act
        String actual = xmlMapper.writeValueAsString(header);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .isEqualTo("<REQ-IF-HEADER IDENTIFIER=\"header-1\"><REQ-IF-TOOL-ID>TailoringExpert</REQ-IF-TOOL-ID><REQ-IF-VERSION>1.2</REQ-IF-VERSION><SOURCE-TOOL-ID>TailoringExpert</SOURCE-TOOL-ID><TITLE>DLR AR-SU Tailoringcatalogue</TITLE></REQ-IF-HEADER>");
    }


}
