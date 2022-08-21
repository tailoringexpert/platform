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
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.TailoringRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.Phase.D;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

class DRDProviderTest {

    DRDProvider provider;
    ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() {
        Map<Phase, Collection<String>> phase2Meilensteine = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(ZERO, unmodifiableCollection(asList("MDR"))),
            new AbstractMap.SimpleEntry<>(A, unmodifiableCollection(asList("PRR", "SRR"))),
            new AbstractMap.SimpleEntry<>(B, unmodifiableCollection(asList("PDR"))),
            new AbstractMap.SimpleEntry<>(C, unmodifiableCollection(asList("CDR"))),
            new AbstractMap.SimpleEntry<>(D, unmodifiableCollection(asList("MRR", "TRR", "QR", "CCB", "MPCB", "AR", "DRB", "DAR", "FRR", "LRR"))),
            new AbstractMap.SimpleEntry<>(E, unmodifiableCollection(asList("AR", "ORR", "GS upgrades", "SW upgrades", "CRR", "ELR"))),
            new AbstractMap.SimpleEntry<>(F, unmodifiableCollection(asList("EOM", "MCR")))
        );

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModules(new ParameterNamesModule(), new JavaTimeModule(), new Jdk8Module());
        this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        this.provider = new DRDProvider(new DRDApplicablePredicate(phase2Meilensteine));
    }


    @Test
    void safety() throws IOException {
        // arrange
        Catalog<TailoringRequirement> catalog;
        try (InputStream is = newInputStream(get("src/test/resources/tailoringcatalog.json"))) {
            assert nonNull(is);
            catalog = objectMapper.readValue(is, new TypeReference<Catalog<TailoringRequirement>>() {
            });
        }

        // act
        Map<DRD, Set<String>> actual = provider.apply(catalog.getChapter("5").get(), asList(E));

        // assert
        assertThat(actual).hasSize(20);
    }

    @Test
    void doit() {
        // arrange
        Chapter<TailoringRequirement> chapter = Chapter.<TailoringRequirement>builder()
            .number("5.1")
            .requirements(asList(
                TailoringRequirement.builder()
                    .text("Requirement 1")
                    .selected(false)
                    .drds(asList(
                        DRD.builder().number("05.01").deliveryDate("SRR").build(),
                        DRD.builder().number("05.02").deliveryDate("PDR").build(),
                        DRD.builder().number("05.03").deliveryDate("QR").build()
                    ))
                    .build(),
                TailoringRequirement.builder()
                    .text("Anforderung2")
                    .selected(true)
                    .drds(asList(
                        DRD.builder().number("05.10").deliveryDate("SRR").build()
                    ))
                    .build(),
                TailoringRequirement.builder()
                    .text("Anforderung3")
                    .selected(true)
                    .drds(asList(
                        DRD.builder().number("05.10").deliveryDate("PDR").build()
                    ))
                    .build()
            ))
            .build();

        // act
        Map<DRD, Set<String>> actual = provider.apply(chapter, asList(A));

        // assert
        assertThat(actual).hasSize(1);
    }
}
