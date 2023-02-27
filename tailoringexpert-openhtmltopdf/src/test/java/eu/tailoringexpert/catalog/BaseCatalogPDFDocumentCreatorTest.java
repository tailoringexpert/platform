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
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
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

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Log4j2
class BaseCatalogPDFDocumentCreatorTest {

    static int mockServerPort = 1080;
    static MockServerClient mockServer;
    KatalogWebServerPortConsumer webServerPortConsumer;
    String templateHome;
    String assetHome;
    ObjectMapper objectMapper;
    FileSaver fileSaver;
    BaseCatalogPDFDocumentCreator creator;

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
        this.templateHome = env.get("TEMPLATE_HOME", "src/test/resources/templates/");
        this.assetHome = env.get("ASSET_HOME", "src/test/resources/templates/");

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.webServerPortConsumer = new KatalogWebServerPortConsumer(1080);

        this.fileSaver = new FileSaver("target");

        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setPrefix(this.templateHome);
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setCharacterEncoding("UTF-8");
        fileTemplateResolver.setOrder(1);

        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(fileTemplateResolver);

        HTMLTemplateEngine templateEngine = new ThymeleafTemplateEngine(
            springTemplateEngine,
            () -> RendererRequestConfiguration.builder()
                .id("unittest")
                .name("plattform")
                .templateHome(this.templateHome)
                .build()
        );

        this.creator = new BaseCatalogPDFDocumentCreator(
            templateEngine,
            new PDFEngine(
                () -> RendererRequestConfiguration.builder()
                    .id("plattform")
                    .name("TailoringExpert")
                    .templateHome(get(this.templateHome).toAbsolutePath().toString())
                    .build()
            )
        );
    }

    @Test
    void createDocument_AllDataAvailable_FileReturned() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }
        webServerPortConsumer.accept(catalog);

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put("PROJEKT", "SAMPLE");
        platzhalter.put("DATUM", now.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        platzhalter.put("DOKUMENT", "DUMMY-XY-Z-1940/DV7");
        platzhalter.put("${DRD_DOCID}", "DUMMY_DOC");

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
        fileSaver.accept("basecatalog.pdf", actual.getData());
    }

    @Test
    void createDocument_CatalogNoToc_NullReturned() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put("PROJEKT", "SAMPLE");
        platzhalter.put("DATUM", now.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        platzhalter.put("DOKUMENT", "DUMMY-XY-Z-1940/DV7");
        platzhalter.put("${DRD_DOCID}", "DUMMY_DOC");


        // act
        File actual = creator.createDocument("4711", catalog, platzhalter);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createDokument_DocIdNull_NullPointerExceptionThrown() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }

        Map<String, Object> platzhalter = new HashMap<>();

        // act
        Throwable actual = catchThrowable(() -> creator.createDocument(null, catalog, platzhalter));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDocument_BaseCatalogNull_NullPointerExceptionThrown() throws Exception {
        // arrange
        Map<String, Object> platzhalter = new HashMap<>();

        // act
        Throwable actual = catchThrowable(() -> creator.createDocument("4711", null, platzhalter));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDocument_PlaceholdersNull_NullPointerExceptionThrown() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<BaseRequirement>>() {
            });
        }

        // act
        Throwable actual = catchThrowable(() -> creator.createDocument("4711", catalog, null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }
}
