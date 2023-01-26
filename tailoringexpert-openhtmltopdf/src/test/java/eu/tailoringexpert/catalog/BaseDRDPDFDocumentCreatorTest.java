/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 Michael Bädorf and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package eu.tailoringexpert.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.KatalogWebServerPortConsumer;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
import eu.tailoringexpert.renderer.ThymeleafTemplateEngine;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.nio.file.Files.readAllBytes;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Log4j2
class BaseDRDPDFDocumentCreatorTest {

    static int mockServerPort = 1080;
    static MockServerClient mockServer;
    KatalogWebServerPortConsumer webServerPortConsumer;
    String templateHome;
    String assetHome;
    ObjectMapper objectMapper;
    FileSaver fileSaver;
    Function<Chapter<BaseRequirement>, Set<DRD>> drdProviderMock;
    BaseDRDPDFDocumentCreator creator;

    @BeforeAll
    static void beforeAll() {
        mockServer = startClientAndServer(mockServerPort);
    }

    @AfterAll
    static void afterAll() {
        mockServer.close();
    }

    @BeforeEach
    void setup() {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        this.templateHome = env.get("TEMPLATE_HOME", "src/test/resources/templates/plattform/");
        this.assetHome = env.get("ASSET_HOME", "src/test/resources/templates/plattform/");

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.webServerPortConsumer = new KatalogWebServerPortConsumer(mockServerPort);

        this.fileSaver = new FileSaver("target");

        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setPrefix(this.templateHome);
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setCharacterEncoding("UTF-8");
        fileTemplateResolver.setOrder(1);

        RendererRequestConfigurationSupplier supplier = () -> RendererRequestConfiguration.builder()
            .id("unittest")
            .name("TailoringExpert")
            .templateHome(this.templateHome)
            .build();

        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(fileTemplateResolver);

        HTMLTemplateEngine templateEngine = new ThymeleafTemplateEngine(springTemplateEngine, supplier);

        this.drdProviderMock = mock(Function.class);
        this.creator = new BaseDRDPDFDocumentCreator(
            templateEngine,
            new PDFEngine(supplier),
            drdProviderMock
        );
    }

    @Test
    void createDocument() throws IOException {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }
        webServerPortConsumer.accept(catalog);


        LocalDateTime now = LocalDateTime.now();
        Map<String, String> platzhalter = new HashMap<>();
        platzhalter.put("PROJEKT", "SAMPLE");
        platzhalter.put("DATUM", now.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        platzhalter.put("DOKUMENT", "SAMPLE-XY-Z-1940/DV7");
        platzhalter.put("${DRD_DOCID}", "SAMPLE_DOC");

        given(drdProviderMock.apply(any()))
            .willReturn(Set.of(
                DRD.builder()
                    .title("Non-Conformance Report (NCR)")
                    .number("03.01")
                    .build())
            );

        mockServer
            .when(request()
                .withMethod("GET")
                .withPath("/assets/.*"))
            .respond(httpRequest -> {
                String asset = httpRequest.getPath().getValue().substring("/assets".length());
                java.io.File file = new java.io.File(this.assetHome + asset);

                return response()
                    .withStatusCode(200)
                    .withBody(readAllBytes(file.toPath()));
            });

        // act
        File actual = creator.createDocument("4711", catalog, platzhalter);

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("basedrds.pdf", actual.getData());
    }

}
