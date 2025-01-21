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
import eu.tailoringexpert.FileSaver;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DocumentSignature;
import eu.tailoringexpert.domain.DocumentSignatureState;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

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
import static java.util.Map.ofEntries;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@Log4j2
class CMRequirementsExcelDocumentCreatorTest {

    private ObjectMapper objectMapper;
    private FileSaver fileSaver;
    BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProviderMock;
    private CMRequirementsExcelDocumentCreator creator;


    @BeforeEach
    void setup() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.fileSaver = new FileSaver("target");

        this.drdProviderMock =
            new DRDProvider(new DRDApplicablePredicate(ofEntries(
                new SimpleEntry<>(ZERO, unmodifiableCollection(asList("MDR"))),
                new SimpleEntry<>(A, unmodifiableCollection(asList("SRR"))),
                new SimpleEntry<>(B, unmodifiableCollection(asList("PDR"))),
                new SimpleEntry<>(C, unmodifiableCollection(asList("CDR"))),
                new SimpleEntry<>(D, unmodifiableCollection(asList("AR", "DRB", "FRR", "LRR"))),
                new SimpleEntry<>(E, unmodifiableCollection(asList("ORR"))),
                new SimpleEntry<>(F, unmodifiableCollection(asList("EOM")))
            )));
        this.creator = new CMRequirementsExcelDocumentCreator(
            () -> RendererRequestConfiguration.builder()
                .id("unittest")
                .name("unittest")
                .templateHome("src/test/resources/")
                .build(), this.drdProviderMock);
    }

    @Test
    void createDocument_AllInputsProvided_FileCreated() throws IOException {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringkatalog.json")) {
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

        Map<String, Object> parameter = ofEntries(
            Map.entry("titel", "DRD Catalog"),
            Map.entry("beschreibung", "BESCHREIBUNG"),
            Map.entry("PROJEKT", "HRWS"),
            Map.entry("DATUM", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.YYYY"))),
            Map.entry("DOKUMENT", "HRWS-RD-PS-1940/DV7")
        );

        // act
        File actual = creator.createDocument("4711", tailoring, parameter);

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("cm.xlsx", actual.getData());
    }

    @Test
    void createDocument_TemplateNotExists_NullReturned() throws IOException {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringkatalog.json")) {
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

        Map<String, Object> parameter = ofEntries(
            Map.entry("titel", "DRD Catalog"),
            Map.entry("beschreibung", "BESCHREIBUNG"),
            Map.entry("PROJEKT", "HRWS"),
            Map.entry("DATUM", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.YYYY"))),
            Map.entry("DOKUMENT", "HRWS-RD-PS-1940/DV7")
        );

        // act
        File actual;
        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.newInputStream(any(Path.class), any())).thenThrow(new FileNotFoundException());
            actual = creator.createDocument("4711", tailoring, parameter);

        }

        // assert
        assertThat(actual).isNull();
    }
}
