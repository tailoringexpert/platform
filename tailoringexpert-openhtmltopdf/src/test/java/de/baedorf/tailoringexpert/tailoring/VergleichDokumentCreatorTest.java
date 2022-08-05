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
import com.openhtmltopdf.util.XRLog;
import de.baedorf.tailoringexpert.FileSaver;
import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.ScreeningSheet;
import de.baedorf.tailoringexpert.domain.ScreeningSheetParameter;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.renderer.HTMLTemplateEngine;
import de.baedorf.tailoringexpert.renderer.PDFEngine;
import de.baedorf.tailoringexpert.renderer.ThymeleafTemplateEngine;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static java.nio.file.Paths.get;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

class VergleichDokumentCreatorTest {

    String templateHome;
    ObjectMapper objectMapper;
    FileSaver fileSaver;
    VergleichDokumentCreator creator;

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

        HTMLTemplateEngine templateEngine = new ThymeleafTemplateEngine(springTemplateEngine);

        this.creator = new VergleichDokumentCreator(
            templateEngine,
            new PDFEngine("TailoringExpert", get(this.templateHome).toAbsolutePath().toString())
        );
    }

    @Test
    void createDokument() throws Exception {
        // arrange
        Katalog<TailoringAnforderung> katalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringkatalog.json")) {
            assert nonNull(is);
            katalog = objectMapper.readValue(is, new TypeReference<Katalog<TailoringAnforderung>>() {
            });
        }

        List<ScreeningSheetParameter> parameters = Arrays.asList(
            ScreeningSheetParameter.builder()
                .bezeichnung("Anwendungcharakter")
                .wert("wissenschaftlich")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Einsatzort")
                .wert("LEO")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Ensatzzweck")
                .wert("Erdbeobachtungssatellit")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Kostenorientierung")
                .wert("150 <= k")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Lebensdauer")
                .wert("15 Jahre < t")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Produkttyp")
                .wert("SAT")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Programmatische Bewertung")
                .wert("erforderlich")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Phase")
                .wert("ZERO, A, B, C, D")
                .build(),

            ScreeningSheetParameter.builder()
                .bezeichnung("Kuerzel")
                .wert("DUMMY")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Abteilung")
                .wert("XY-Z")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Kurzname")
                .wert("DUMMY")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Langname")
                .wert("Dummy Projekt")
                .build(),
            ScreeningSheetParameter.builder()
                .bezeichnung("Projektleiter")
                .wert("Tim Mälzer")
                .build()
        );
        ScreeningSheet screeningSheet = ScreeningSheet.builder()
            .parameters(parameters)
            .build();

        Tailoring tailoring = Tailoring.builder()
            .katalog(katalog)
            .screeningSheet(screeningSheet)
            .build();
        // act
        Datei actual = creator.createDokument("4711", tailoring, Collections.emptyMap());

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("vergleich.pdf", actual.getBytes());

    }
}
