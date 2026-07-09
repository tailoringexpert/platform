/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import static java.lang.Boolean.TRUE;
import static java.util.Map.entry;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
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
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.File;
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
import tools.jackson.databind.ObjectMapper;

@Log4j2
class TailoringsDiffPDFDocumentCreatorTest {

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
        Tailoring master = load.apply("src/test/resources/master.json");
        assert nonNull(master);

        Tailoring master1 = load.apply("src/test/resources/master1.json");
        assert nonNull(master1);

        Map<String, Object> parameters = Map.ofEntries(
                entry("BASE_PROJECT", "Diff Demo"),
                entry("BASE_TAILORING", "master"),
                entry("COMPARE_PROJECT", "Baseline "),
                entry("COMPARE_TAILORING", "master2"));

        // act
        File actual = requirementDiff.createDocument("docId", master, master1, parameters);

        // assert
        assertThat(actual.getData()).isNotNull();
        fileSaver.accept("diff.pdf", actual.getData());
    }

    @Test
    void apply() {
        // arrange
        Tailoring master = Tailoring.builder()
                .catalog(Catalog.<TailoringRequirement>builder()
                        .toc(Chapter.<TailoringRequirement>builder()
                                .chapters(List.of(
                                        Chapter.<TailoringRequirement>builder()
                                                .number("1")
                                                .chapters(List.of(
                                                        Chapter.<TailoringRequirement>builder()
                                                                .number("1.1")
                                                                .chapters(List.of(
                                                                        Chapter.<TailoringRequirement>builder()
                                                                                .number("1.1.1")
                                                                                .requirements(List.of(
                                                                                        TailoringRequirement.builder()
                                                                                                .position("a")
                                                                                                .text("Hallo 1.1.1a")
                                                                                                .selected(TRUE)
                                                                                                .build()))
                                                                                .build()))
                                                                .requirements(List.of(
                                                                        TailoringRequirement.builder()
                                                                                .position("a")
                                                                                .text("Hallo 1.1a")
                                                                                .selected(TRUE)
                                                                                .build()))
                                                                .build(),
                                                        Chapter.<TailoringRequirement>builder()
                                                                .number("1.2")
                                                                .requirements(List.of(
                                                                        TailoringRequirement.builder()
                                                                                .position("a")
                                                                                .text("Hallo 1.2a")
                                                                                .selected(TRUE)
                                                                                .build()))
                                                                .build()))
                                                .requirements(List.of())
                                                .build())

                                )
                                .requirements(List.of())
                                .build())
                        .build())
                .build();

        Tailoring master1 = Tailoring.builder()
                .catalog(Catalog.<TailoringRequirement>builder()
                        .toc(Chapter.<TailoringRequirement>builder()
                                .chapters(List.of(
                                        Chapter.<TailoringRequirement>builder()
                                                .number("1")
                                                .chapters(List.of(
                                                        Chapter.<TailoringRequirement>builder()
                                                                .number("1.1")
                                                                .requirements(List.of())
                                                                .build(),
                                                        Chapter.<TailoringRequirement>builder()
                                                                .number("1.2")
                                                                .requirements(List.of(
                                                                        TailoringRequirement.builder()
                                                                                .position("a")
                                                                                .text("Hallo 1.2a")
                                                                                .selected(TRUE)
                                                                                .build()))
                                                                .build()))
                                                .requirements(List.of())
                                                .build())

                                )
                                .requirements(List.of())
                                .build())
                        .build())
                .build();

        // act
        Map<String, List<TailoringRequirementDiff>> actual = new LinkedHashMap<>();
        requirementDiff.apply(master.getCatalog().getToc(), master1.getCatalog().getToc(), actual);
        actual.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        // assert
        log.info(actual);
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
