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
import com.openhtmltopdf.util.XRLog;
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.ScreeningSheetParameter;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
import eu.tailoringexpert.renderer.ThymeleafTemplateEngine;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

class ComparisonPDFDocumentCreatorTest {

    String templateHome;
    ObjectMapper objectMapper;
    FileSaver fileSaver;
    ComparisonPDFDocumentCreator creator;

    @BeforeAll
    static void beforeAll() {
        XRLog.listRegisteredLoggers().forEach(logger -> XRLog.setLevel(logger, Level.FINEST));
    }

    @BeforeEach
    void setup() {
        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        this.templateHome = env.get("TEMPLATE_HOME", "src/test/resources/templates/");

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

        SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
        springTemplateEngine.addTemplateResolver(fileTemplateResolver);

        RendererRequestConfigurationSupplier supplier = () -> RendererRequestConfiguration.builder()
            .id("unittest")
            .name("TailoringExpert")
            .templateHome(this.templateHome)
            .build();
        HTMLTemplateEngine templateEngine = new ThymeleafTemplateEngine(springTemplateEngine, supplier);

        this.creator = new ComparisonPDFDocumentCreator(templateEngine, new PDFEngine(supplier));
    }

    @Test
    void createDocument_ValidInput_FileCreated() throws Exception {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringcatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<TailoringRequirement>>() {
            });
        }

        List<ScreeningSheetParameter> parameters = Arrays.asList(
            ScreeningSheetParameter.builder()
                .category("Anwendungcharakter")
                .value("wissenschaftlich")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Einsatzort")
                .value("LEO")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Ensatzzweck")
                .value("Erdbeobachtungssatellit")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Kostenorientierung")
                .value("150 <= k")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Lebensdauer")
                .value("15 Jahre < t")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Produkttyp")
                .value("SAT")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Programmatische Bewertung")
                .value("erforderlich")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Phase")
                .value("ZERO, A, B, C, D")
                .build(),

            ScreeningSheetParameter.builder()
                .category("Identifier")
                .value("DUMMY")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Abteilung")
                .value("XY-Z")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Kurzname")
                .value("DUMMY")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Langname")
                .value("Dummy Project")
                .build(),
            ScreeningSheetParameter.builder()
                .category("Projektleiter")
                .value("Tim Mälzer")
                .build()
        );
        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(parameters)
            .build();

        Tailoring tailoring = Tailoring.builder()
            .catalog(catalog)
            .screeningSheet(screeningSheet)
            .build();
        // act
        File actual = creator.createDocument("4711", tailoring, Collections.emptyMap());

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("comparision.pdf", actual.getData());

    }

    @Test
    void createDocument_DocIdNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Exception actual = catchException(() -> creator.createDocument(null, Tailoring.builder().build(), Collections.emptyMap()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDocument_TailoringNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Exception actual = catchException(() -> creator.createDocument("DOC-4711", null, Collections.emptyMap()));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void createDocument_PlaceholdersNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Exception actual = catchException(() -> creator.createDocument("DOC-4711", Tailoring.builder().build(), null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }
}
