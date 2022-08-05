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
package de.baedorf.tailoringexpert.tailoring;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import de.baedorf.tailoringexpert.FileSaver;
import de.baedorf.tailoringexpert.KatalogWebServerPortConsumer;
import de.baedorf.tailoringexpert.renderer.HTMLTemplateEngine;
import de.baedorf.tailoringexpert.renderer.PDFEngine;
import de.baedorf.tailoringexpert.renderer.ThymeleafTemplateEngine;
import de.baedorf.tailoringexpert.domain.DRD;
import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.DokumentZeichnung;
import de.baedorf.tailoringexpert.domain.DokumentZeichnungStatus;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.Phase;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static de.baedorf.tailoringexpert.domain.Phase.A;
import static de.baedorf.tailoringexpert.domain.Phase.B;
import static de.baedorf.tailoringexpert.domain.Phase.C;
import static de.baedorf.tailoringexpert.domain.Phase.D;
import static de.baedorf.tailoringexpert.domain.Phase.E;
import static de.baedorf.tailoringexpert.domain.Phase.F;
import static de.baedorf.tailoringexpert.domain.Phase.ZERO;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.List.of;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Log4j2
class CMDokumentCreatorTest {

    static int mockServerPort = 1080;
    static MockServerClient mockServer;
    KatalogWebServerPortConsumer webServerPortConsumer;
    String templateHome;
    String assetHome;
    ObjectMapper objectMapper;
    FileSaver fileSaver;
    BiFunction<Kapitel<TailoringAnforderung>, Collection<Phase>, Map<DRD, Set<String>>> drdProviderMock;
    CMDokumentCreator creator;

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

        this.webServerPortConsumer = new KatalogWebServerPortConsumer(mockServerPort);

        this.fileSaver = new FileSaver("target");

        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setPrefix(this.templateHome);
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setCharacterEncoding("UTF-8");
        fileTemplateResolver.setOrder(1);

        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(fileTemplateResolver);

        HTMLTemplateEngine templateEngine = new ThymeleafTemplateEngine(springTemplateEngine);

        this.drdProviderMock = new DRDProvider(new DRDAnwendbarPraedikat(Map.ofEntries(
            new AbstractMap.SimpleEntry<>(ZERO, unmodifiableCollection(asList("MDR"))),
            new AbstractMap.SimpleEntry<>(A, unmodifiableCollection(asList("SRR"))),
            new AbstractMap.SimpleEntry<>(B, unmodifiableCollection(asList("PDR"))),
            new AbstractMap.SimpleEntry<>(C, unmodifiableCollection(asList("CDR"))),
            new AbstractMap.SimpleEntry<>(D, unmodifiableCollection(asList("AR", "DRB", "FRR", "LRR"))),
            new AbstractMap.SimpleEntry<>(E, unmodifiableCollection(asList("ORR"))),
            new AbstractMap.SimpleEntry<>(F, unmodifiableCollection(asList("EOM")))
        )));
        this.creator = new CMDokumentCreator(
            templateEngine,
            new PDFEngine("TailoringExpert", get(this.templateHome).toAbsolutePath().toString()),
            drdProviderMock
        );
    }

    @Test
    void createDokument() throws IOException {
        // arrange
        Katalog<TailoringAnforderung> katalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringkatalog.json")) {
            assert nonNull(is);
            katalog = objectMapper.readValue(is, new TypeReference<Katalog<TailoringAnforderung>>() {
            });
        }
        webServerPortConsumer.accept(katalog);

        Collection<DokumentZeichnung> zeichnungen = of(
            DokumentZeichnung.builder()
                .anwendbar(true)
                .bereich("Sofware")
                .unterzeichner("Hans Dampf")
                .status(DokumentZeichnungStatus.AGREED)
                .build()
        );

        Tailoring tailoring = Tailoring.builder()
            .katalog(katalog)
            .zeichnungen(zeichnungen)
            .phasen(of(ZERO, A, B, C, D, E, F))
            .build();

        LocalDateTime now = LocalDateTime.now();
        Map<String, String> platzhalter = new HashMap<>();
        platzhalter.put("PROJEKT", "SAMPLE");
        platzhalter.put("DATUM", now.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        platzhalter.put("DOKUMENT", "SAMPLE-XY-Z-1940/DV7");
        platzhalter.put("${DRD_DOCID}", "SAMPLE_DOC");

        mockServer
            .when(request()
                .withMethod("GET")
                .withPath("/assets/.*"))
            .respond(httpRequest -> {
                String asset = httpRequest.getPath().getValue().substring("/assets".length());
                File file = new File(this.assetHome + asset);

                return response()
                    .withStatusCode(200)
                    .withBody(readAllBytes(file.toPath()));
            });

        // act
        Datei actual = creator.createDokument("4711", tailoring, platzhalter);

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("cm.pdf", actual.getBytes());
    }
}
