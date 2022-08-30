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
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.BiConsumer;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.Phase.D;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
class TailoringCatalogExcelDocumentCreatorTest {

    TailoringCatalogExcelDocumentCreator creator;
    ObjectMapper objectMapper;
    BiConsumer<String, byte[]> fileSaver = (dateiName, data) -> {
        try {
            Path path = Paths.get("target", dateiName);
            OutputStream out = Files.newOutputStream(path);
            out.write(data);
            out.close();
        } catch (Exception e) {
            log.catching(e);
        }
    };

    @BeforeEach
    void beforeEach() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.creator = new TailoringCatalogExcelDocumentCreator();

    }

    @Test
    void createDokument_TailoringExists_FileCreated() throws Exception {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = this.getClass().getResourceAsStream("/tailoringkatalog.json")) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<TailoringRequirement>>() {
            });
        }

        Tailoring tailoring = Tailoring.builder()
            .catalog(catalog)
            .signatures(emptyList())
            .phases(Arrays.asList(ZERO, A, B, C, D, E, F))
            .build();

        // act
        File actual = creator.createDocument("42", tailoring, emptyMap());

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("43.xlsx", actual.getData());

    }

    @Test
    void createDocument_NoToc_EmptyFileCreated() throws Exception {
        // arrange
        Tailoring tailoring = Tailoring.builder()
            .catalog(Catalog.<TailoringRequirement>builder().build())
            .signatures(emptyList())
            .phases(Arrays.asList(ZERO, A, B, C, D, E, F))
            .build();

        // act
        File actual = creator.createDocument("42", tailoring, emptyMap());

        // assert
        assertThat(actual).isNotNull();
        fileSaver.accept("leer.xlsx", actual.getData());
    }

    @Test
    void createDocument_ChapterNull_NullReturned() throws Exception {
        // arrange
        Tailoring tailoring = Tailoring.builder()
            .catalog(Catalog.<TailoringRequirement>builder()
                .toc(Chapter.<TailoringRequirement>builder()
                    .chapters(null)
                    .build())
                .build())

            .signatures(emptyList())
            .phases(Arrays.asList(ZERO, A, B, C, D, E, F))
            .build();

        // act
        File actual = creator.createDocument("42", tailoring, emptyMap());

        // assert
        assertThat(actual).isNull();
    }
}
