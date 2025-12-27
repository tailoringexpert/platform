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
package eu.tailoringexpert.tailoring;

import com.openhtmltopdf.extend.FSDOMMutator;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
import eu.tailoringexpert.renderer.TailoringexpertDOMMutator;
import eu.tailoringexpert.renderer.ThymeleafTemplateEngine;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static java.util.Collections.emptySet;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Log4j2
class DRDPDFDocumentCreatorTest {

    String templateHome;
    JsonMapper objectMapper;
    FileSaver fileSaver;
    BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProviderMock;
    DRDPDFDocumentCreator creator;

    @BeforeEach
    void setup() {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        this.templateHome = env.get("TEMPLATE_HOME", "src/test/resources/templates/");

        this.objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

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

        this.drdProviderMock = mock(BiFunction.class);
        FSDOMMutator domMutator = new TailoringexpertDOMMutator();
        this.creator = new DRDPDFDocumentCreator(
            drdProviderMock,
            templateEngine,
            new PDFEngine(domMutator, supplier)
        );
    }

    @Test
    void createDocument_ValidInput_FileCreated() throws IOException {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringcatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, TailoringRequirement.class)
            );
        }

        Tailoring tailoring = Tailoring.builder()
            .catalog(catalog)
            .build();

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> platzhalter = new HashMap<>();
        platzhalter.put("PROJEKT", "SAMPLE");
        platzhalter.put("DATUM", now.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        platzhalter.put("DOKUMENT", "SAMPLE-XY-Z-1940/DV7");
        platzhalter.put("${DRD_DOCID}", "SAMPLE_DOC");

        given(drdProviderMock.apply(any(), any()))
            .willReturn(ofEntries(
                    entry(
                        DRD.builder()
                            .title("Non-Conformance Report (NCR)")
                            .number("03.01")
                            .build(),
                        emptySet())
                )
            );


        // act
        File actual = creator.createDocument("4711", tailoring, platzhalter);

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("drd.pdf", actual.getData());
    }

}
