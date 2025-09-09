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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.openhtmltopdf.extend.FSDOMMutator;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.catalog.RequirementAlwaysSelectedPredicate;
import eu.tailoringexpert.domain.*;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
import eu.tailoringexpert.renderer.TailoringexpertDOMMutator;
import eu.tailoringexpert.renderer.ThymeleafTemplateEngine;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.Phase.D;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.List.of;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class TailoringCatalogPDFDocumentCreatorTest {

    String templateHome;
    String assetHome;
    DRDProvider<TailoringRequirement> drdProviderMock;
    ApplicableDocumentProvider<TailoringRequirement> applicableDocumentProviderMock;
    ObjectMapper objectMapper;
    FileSaver fileSaver;
    TailoringCatalogPDFDocumentCreator creator;

    @BeforeEach
    void setup() {
        Dotenv env = Dotenv.configure().systemProperties().ignoreIfMissing().load();

        this.templateHome = env.get("TEMPLATE_HOME", "src/test/resources/templates/");
        this.assetHome = env.get("ASSET_HOME", "src/test/resources/templates/");
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

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

        this.drdProviderMock = new DRDProvider(
            (Predicate<TailoringRequirement>) requirement -> ((TailoringRequirement) requirement).getSelected(),new DRDApplicablePredicate(Map.ofEntries(
            new SimpleEntry<>(ZERO, unmodifiableCollection(asList("MDR"))),
            new SimpleEntry<>(A, unmodifiableCollection(asList("SRR"))),
            new SimpleEntry<>(B, unmodifiableCollection(asList("PDR"))),
            new SimpleEntry<>(C, unmodifiableCollection(asList("CDR"))),
            new SimpleEntry<>(D, unmodifiableCollection(asList("AR", "DRB", "FRR", "LRR"))),
            new SimpleEntry<>(E, unmodifiableCollection(asList("ORR"))),
            new SimpleEntry<>(F, unmodifiableCollection(asList("EOM")))
        )));

        this.applicableDocumentProviderMock = new ApplicableDocumentProvider(
            new RequirementSelectedPredicate(),
            new DocumentNumberComparator());

        FSDOMMutator domMutator = new TailoringexpertDOMMutator();
        this.creator = new TailoringCatalogPDFDocumentCreator(
            drdProviderMock,
            applicableDocumentProviderMock,
            templateEngine,
        new PDFEngine(domMutator, supplier)
        );
    }

    @Test
    void createDocument_ImgAbsoulteAndWithUrl_FileCreated() throws Exception {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringcatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<TailoringRequirement>>() {
            });
        }

        Collection<DocumentSignature> zeichnungen = of(
            DocumentSignature.builder()
                .applicable(true)
                .faculty("Software")
                .signee("Hans Dampf")
                .state(DocumentSignatureState.AGREED)
                .build()
        );

        Tailoring tailoring = Tailoring.builder()
            .catalog(catalog)
            .signatures(zeichnungen)
            .phases(of(ZERO, A, B, C, D, E, F))
            .build();

        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("PROJEKT", "SAMPLE");
        ctx.put("DATUM", now.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        ctx.put("DOKUMENT", "SAMPLE-XY-Z-1940/DV7");
        ctx.put("${DRD_DOCID}", "SAMPLE_DOC");
        ctx.put("SHOW_ALL", Boolean.FALSE);

        // act
        File actual = creator.createDocument("4711", tailoring, ctx);

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("tailoringcatalog.pdf", actual.getData());
    }

}
