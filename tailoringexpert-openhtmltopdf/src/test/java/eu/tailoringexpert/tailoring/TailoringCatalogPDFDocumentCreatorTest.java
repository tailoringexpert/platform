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
import eu.tailoringexpert.domain.*;
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

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.Phase.D;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.List.of;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Log4j2
class TailoringCatalogPDFDocumentCreatorTest {

    String templateHome;
    String assetHome;
    DRDProvider<TailoringRequirement> drdProviderMock;
    ApplicableDocumentProvider<TailoringRequirement> applicableDocumentProviderMock;
    JsonMapper objectMapper;
    FileSaver fileSaver;
    TailoringCatalogPDFDocumentCreator creator;

    @BeforeEach
    void setup() {
        Dotenv env = Dotenv.configure().systemProperties().ignoreIfMissing().load();

        this.templateHome = env.get("TEMPLATE_HOME", "src/test/resources/templates/");
        this.assetHome = env.get("ASSET_HOME", "src/test/resources/templates/");

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

        this.drdProviderMock = new DRDProvider(
            (Predicate<TailoringRequirement>) requirement -> ((TailoringRequirement) requirement).getSelected(), new DRDApplicablePredicate(Map.ofEntries(
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
            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, TailoringRequirement.class)
            );
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

    /**
     * #458
     */
    @Test
    void addRequirement_458_RowWithReferenceDataAdded() {
        // arrange
        TailoringRequirement requirement = TailoringRequirement.builder()
            .position("a")
            .text("Sample Text")
            .selected(true)
            .reference(Reference.builder()
                .changed(false)
                .text("Referencetext")
                .logo(Logo.builder()
                    .name("demo")
                    .url("demo_logo.png")
                    .build())
                .build())
            .build();

        Map<String, Object> ctx = new HashMap<>();
        List<CatalogElement> rows = new ArrayList<>();

        // act
        creator.addRequirement(requirement, rows, ctx);

        // assert
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().getReference()).isEqualTo("Referencetext");
        assertThat(rows.getFirst().getLogo()).isEqualTo("demo_logo.png");
    }
}
