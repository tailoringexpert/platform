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

import com.openhtmltopdf.extend.FSDOMMutator;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.domain.ApplicableDocumentProvider;
import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.DRDProvider;
import eu.tailoringexpert.domain.DocumentNumberComparator;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
import eu.tailoringexpert.renderer.TailoringexpertDOMMutator;
import eu.tailoringexpert.renderer.ThymeleafTemplateEngine;
import eu.tailoringexpert.tailoring.DRDApplicablePredicate;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
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
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@Log4j2
class BaseCatalogPDFDocumentCreatorTest {

    String templateHome;
    String assetHome;
    JsonMapper objectMapper;
    FileSaver fileSaver;
    BaseCatalogPDFDocumentCreator creator;

    @BeforeEach
    void setup() {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        this.templateHome = env.get("TEMPLATE_HOME", "src/test/resources/templates/");

        this.objectMapper = JsonMapper.builder()
            .findAndAddModules()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

        this.fileSaver = new FileSaver("target");

        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setPrefix(this.templateHome);
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setCharacterEncoding("UTF-8");
        fileTemplateResolver.setOrder(1);

        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(fileTemplateResolver);

        RendererRequestConfigurationSupplier supplier = () -> RendererRequestConfiguration.builder()
            .id("unittest")
            .name("platform")
            .templateHome(this.templateHome)
            .build();

        HTMLTemplateEngine templateEngine = new ThymeleafTemplateEngine(
            springTemplateEngine, supplier
        );

        FSDOMMutator domMutator = new TailoringexpertDOMMutator();


        this.creator = new BaseCatalogPDFDocumentCreator(
            new ApplicableDocumentProvider<BaseRequirement>(new RequirementAlwaysSelectedPredicate<BaseRequirement>(), new DocumentNumberComparator()),
            new DRDProvider<>(
                (Predicate<BaseRequirement>) requirement -> true,
                new DRDApplicablePredicate(Map.ofEntries(
                    new SimpleEntry<>(ZERO, unmodifiableCollection(asList("MDR"))),
                    new SimpleEntry<>(A, unmodifiableCollection(asList("PRR", "SRR"))),
                    new SimpleEntry<>(B, unmodifiableCollection(asList("PDR"))),
                    new SimpleEntry<>(C, unmodifiableCollection(asList("CDR"))),
                    new SimpleEntry<>(D, unmodifiableCollection(asList("MRR", "TRR", "QR", "CCB", "MPCB", "AR", "DRB", "DAR", "FRR", "LRR"))),
                    new SimpleEntry<>(E, unmodifiableCollection(asList("AR", "ORR", "GS upgrades", "SW upgrades", "CRR", "ELR"))),
                    new SimpleEntry<>(F, unmodifiableCollection(asList("EOM", "MCR")))
                ))
            ),
            templateEngine,
            new PDFEngine(domMutator, supplier)
        );
    }

    @Test
    void createDocument_ValidInput_FileCreated() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/catalog_8.2.2.json")) {
            assert nonNull(is);

            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, BaseRequirement.class)
            );
        }

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("PROJEKT", "SAMPLE");
        ctx.put("DATUM", now.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        ctx.put("DOKUMENT", "DUMMY-XY-Z-1940/DV7");
        ctx.put("${DRD_DOCID}", "DUMMY_DOC");


        // act
        File actual = creator.createDocument("4711", catalog, ctx);

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("basecatalog.pdf", actual.getData());
    }

    @Test
    void createDocument_CatalogNoToc_NullReturned() {
        // arrange
        Catalog<BaseRequirement> catalog = Catalog.<BaseRequirement>builder().build();

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("PROJEKT", "SAMPLE");
        ctx.put("DATUM", now.format(DateTimeFormatter.ofPattern("dd.MM.YYYY")));
        ctx.put("DOKUMENT", "DUMMY-XY-Z-1940/DV7");
        ctx.put("${DRD_DOCID}", "DUMMY_DOC");


        // act
        File actual = creator.createDocument("4711", catalog, ctx);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void createDokument_DocIdNull_NullPointerExceptionThrown() throws Exception {
        // arrange
        Catalog<BaseRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/basecatalog.json")) {
            assert nonNull(is);

            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, BaseRequirement.class)
            );
        }

        Map<String, Object> platzhalter = new HashMap<>();

        // act
        Throwable actual = catchThrowable(() -> creator.createDocument(null, catalog, platzhalter));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDocument_BaseCatalogNull_NullPointerExceptionThrown() {
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

            catalog = objectMapper.readValue(
                is,
                objectMapper.getTypeFactory()
                    .constructParametricType(Catalog.class, BaseRequirement.class)
            );
        }

        // act
        Throwable actual = catchThrowable(() -> creator.createDocument("4711", catalog, null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }
}
