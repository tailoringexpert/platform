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
import de.baedorf.tailoringexpert.domain.DRD;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.Phase;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static de.baedorf.tailoringexpert.domain.Phase.A;
import static de.baedorf.tailoringexpert.domain.Phase.B;
import static de.baedorf.tailoringexpert.domain.Phase.C;
import static de.baedorf.tailoringexpert.domain.Phase.D;
import static de.baedorf.tailoringexpert.domain.Phase.E;
import static de.baedorf.tailoringexpert.domain.Phase.F;
import static de.baedorf.tailoringexpert.domain.Phase.ZERO;
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

        this.provider = new DRDProvider(new DRDAnwendbarPraedikat(phase2Meilensteine));
    }


    @Test
    void safety() throws IOException {
        // arrange
        Katalog<TailoringAnforderung> katalog;
        try (InputStream is = newInputStream(get("src/test/resources/tailoringkatalog.json"))) {
            assert nonNull(is);
            katalog = objectMapper.readValue(is, new TypeReference<Katalog<TailoringAnforderung>>() {
            });
        }

        // act
        Map<DRD, Set<String>> actual = provider.apply(katalog.getKapitel("5").get(), asList(E));

        // assert
        assertThat(actual).hasSize(20);
    }

    @Test
    void doit() {
        // arrange
        Kapitel<TailoringAnforderung> kapitel = Kapitel.<TailoringAnforderung>builder()
            .nummer("5.1")
            .anforderungen(asList(
                TailoringAnforderung.builder()
                    .text("Anforderung 1")
                    .ausgewaehlt(false)
                    .drds(asList(
                        DRD.builder().nummer("05.01").lieferzeitpunkt("SRR").build(),
                        DRD.builder().nummer("05.02").lieferzeitpunkt("PDR").build(),
                        DRD.builder().nummer("05.03").lieferzeitpunkt("QR").build()
                    ))
                    .build(),
                TailoringAnforderung.builder()
                    .text("Anforderung2")
                    .ausgewaehlt(true)
                    .drds(asList(
                        DRD.builder().nummer("05.10").lieferzeitpunkt("SRR").build()
                    ))
                    .build(),
                TailoringAnforderung.builder()
                    .text("Anforderung3")
                    .ausgewaehlt(true)
                    .drds(asList(
                        DRD.builder().nummer("05.10").lieferzeitpunkt("PDR").build()
                    ))
                    .build()
            ))
            .build();

        // act
        Map<DRD, Set<String>> actual = provider.apply(kapitel, asList(A));

        // assert
        assertThat(actual).hasSize(1);
    }
}
