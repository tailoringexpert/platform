package eu.tailoringexpert.tailoring;

import static java.util.Map.entry;
import static java.util.Objects.nonNull;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import com.github.difflib.text.DiffRowGenerator;
import com.openhtmltopdf.extend.FSDOMMutator;
import com.openhtmltopdf.extend.FSObjectDrawerFactory;
import com.openhtmltopdf.render.DefaultObjectDrawerFactory;

import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
import eu.tailoringexpert.renderer.TailoringexpertDOMMutator;
import eu.tailoringexpert.renderer.ThymeleafTemplateEngine;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.ObjectMapper;

@Log4j2
public class TailoringsDiffPDFDocumentCreatorTest {

    ObjectMapper objectMapper;
    FileSaver fileSaver;

    DiffRowGenerator generator;
    TailoringsDiffPDFDocumentCreator requirementDiff;

    @BeforeEach
    void BeforeEach() {
        Dotenv env = Dotenv.configure().systemProperties().ignoreIfMissing().load();
        String templateHome = env.get("TEMPLATE_HOME", "src/test/resources/templates/");

        this.objectMapper = new ObjectMapper();
        this.fileSaver = new FileSaver("target");

        this.generator = DiffRowGenerator.create()
                .reportLinesUnchanged(false)
                .showInlineDiffs(true)
                .mergeOriginalRevised(false)
                .inlineDiffByWord(true)
                .ignoreWhiteSpaces(true)
                .lineNormalizer(Function.identity())
                // .oldTag((tag, f) -> f ? "<span class='requirement-old'>" : "</span>")
                .newTag((tag, f) -> f ? "<span class='requirement-new'>" : "</span>")
                .build();

        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setPrefix(templateHome);
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setCharacterEncoding("UTF-8");
        fileTemplateResolver.setOrder(1);

        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(fileTemplateResolver);
        RendererRequestConfigurationSupplier supplier = () -> RendererRequestConfiguration.builder()
                .id("unittest")
                .name("TailoringExpert")
                .templateHome(templateHome)
                .build();
        HTMLTemplateEngine templateEngine = new ThymeleafTemplateEngine(springTemplateEngine, supplier);
        FSDOMMutator domMutator = new TailoringexpertDOMMutator();
        FSObjectDrawerFactory objectDrawerFactory = new DefaultObjectDrawerFactory();

        this.requirementDiff = new TailoringsDiffPDFDocumentCreator(
                new TailoringRequirmentTextDiffProvider(generator, templateEngine),
                templateEngine,
                new PDFEngine(domMutator, objectDrawerFactory, supplier));
    }

    @Test
    void createDocument_TailoringWithDiffs_FileCreated() {
        // arrage
        Tailoring master = load.apply("src/test/resources/tailoringcatalog.json");
        assert nonNull(master);

        Tailoring master1 = load.apply("src/test/resources/rau10.json");
        assert nonNull(master1);

        Map<String, Object> parameters = Map.ofEntries(
                entry("BASE_PROJECT", "Diff Demo"),
                entry("BASE_TAILORING", "master"),
                entry("COMPARE_PROJECT", "Baseline "),
                entry("COMPARE_TAILORING", "master2"));

        // act
        File actual = requirementDiff.createDocument(
                master, master1, parameters);

        // assert
        fileSaver.accept("diff.pdf", actual.getData());
    }

    Function<String, Tailoring> load = name -> {
        try (InputStream is = Files.newInputStream(Paths.get(name))) {
            assert nonNull(is);
            return objectMapper.readValue(is, Tailoring.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    };

}
