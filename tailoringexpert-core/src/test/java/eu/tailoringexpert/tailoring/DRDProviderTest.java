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

import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.TailoringRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DRDProviderTest {

    DRDProvider provider;

    BiPredicate<String, Collection<Phase>> predicateMock;

    @BeforeEach
    void beforeEach() {
        this.predicateMock = Mockito.mock(BiPredicate.class);
        this.provider = new DRDProvider(this.predicateMock);
    }


    @Test
    void apply_DRDNotInPhases_EmptyMapReturned() {
        // arrange
        // arrange
        DRD drd0101 = createDRD("01.01", "CDR");
        DRD drd0102 = createDRD("01.02", "PDR");
        TailoringRequirement requirement0101 = TailoringRequirement.builder()
            .selected(true)
            .drds(of(drd0101, drd0102))
            .build();

        DRD drd0103 = createDRD("01.03", "CRR");
        TailoringRequirement requirement0102 = TailoringRequirement.builder()
            .selected(false)
            .drds(of(drd0103))
            .build();

        DRD drd1101 = createDRD("11.01", "FAR");
        TailoringRequirement requirement1101 = TailoringRequirement.builder()
            .selected(true)
            .drds(of(drd1101))
            .build();

        Catalog<TailoringRequirement> catalog = Catalog.<TailoringRequirement>builder()
            .toc(Chapter.<TailoringRequirement>builder()
                .name("/")
                .chapters(of(
                        Chapter.<TailoringRequirement>builder()
                            .number("1")
                            .requirements(of(
                                requirement0101,
                                requirement0102
                            ))
                            .chapters(of(
                                Chapter.<TailoringRequirement>builder()
                                    .number("1.1")
                                    .requirements(of(
                                        requirement1101
                                    ))
                                    .build()

                            ))
                            .build()
                    )
                ).build()
            )
            .build();

        List<Phase> phases = Collections.emptyList();


        // act
        Map<DRD, Set<String>> actual = provider.apply(catalog.getChapter("1").get(), phases);

        // assert
        assertThat(actual).isEmpty();
        verify(predicateMock, times(3)).test(anyString(), eq(phases));
    }

    @Test
    void apply_AllSelectedRequiremensInPhases_MapWithDRDsReturned() {
        // arrange
        DRD drd0101 = createDRD("01.01", "CDR");
        DRD drd0102 = createDRD("01.02", "PDR");
        TailoringRequirement requirement0101 = TailoringRequirement.builder()
            .selected(true)
            .drds(of(drd0101, drd0102))
            .build();

        DRD drd0103 = createDRD("01.03", "CRR");
        TailoringRequirement requirement0102 = TailoringRequirement.builder()
            .selected(false)
            .drds(of(drd0103))
            .build();

        DRD drd1101 = createDRD("11.01", "FAR");
        TailoringRequirement requirement1101 = TailoringRequirement.builder()
            .selected(true)
            .drds(of(drd1101))
            .build();

        Catalog<TailoringRequirement> catalog = Catalog.<TailoringRequirement>builder()
            .toc(Chapter.<TailoringRequirement>builder()
                .name("/")
                .chapters(of(
                        Chapter.<TailoringRequirement>builder()
                            .number("1")
                            .requirements(of(
                                requirement0101,
                                requirement0102
                            ))
                            .chapters(of(
                                Chapter.<TailoringRequirement>builder()
                                    .number("1.1")
                                    .requirements(of(
                                        requirement1101
                                    ))
                                    .build()

                            ))
                            .build()
                    )
                ).build()
            )
            .build();

        List<Phase> phases = of(
            Phase.ZERO,
            Phase.A,
            Phase.B,
            Phase.C,
            Phase.D,
            Phase.E,
            Phase.F
        );
        given(predicateMock.test("CDR", phases)).willReturn(true);
        given(predicateMock.test("PDR", phases)).willReturn(true);
        given(predicateMock.test("FAR", phases)).willReturn(true);


        // act
        Map<DRD, Set<String>> actual = provider.apply(catalog.getChapter("1").get(), phases);

        // assert
        assertThat(actual)
            .hasSize(3)
            .containsOnlyKeys(drd0101, drd0102, drd1101);

        verify(predicateMock, times(3)).test(anyString(), eq(phases));
    }

    @Test
    void apply_NotAllSelectedRequiremensInPhases_MapWithDRDsReturned() {
        // arrange
        DRD drd0101 = createDRD("01.01", "CDR");
        DRD drd0102 = createDRD("01.02", "PDR");
        TailoringRequirement requirement0101 = TailoringRequirement.builder()
            .selected(true)
            .drds(of(drd0101, drd0102))
            .build();

        DRD drd0103 = createDRD("01.03", "CRR");
        TailoringRequirement requirement0102 = TailoringRequirement.builder()
            .selected(false)
            .drds(of(drd0103))
            .build();

        DRD drd1101 = createDRD("11.01", "FAR");
        TailoringRequirement requirement1101 = TailoringRequirement.builder()
            .selected(true)
            .drds(of(drd1101))
            .build();

        Catalog<TailoringRequirement> catalog = Catalog.<TailoringRequirement>builder()
            .toc(Chapter.<TailoringRequirement>builder()
                .name("/")
                .chapters(of(
                        Chapter.<TailoringRequirement>builder()
                            .number("1")
                            .requirements(of(
                                requirement0101,
                                requirement0102
                            ))
                            .chapters(of(
                                Chapter.<TailoringRequirement>builder()
                                    .number("1.1")
                                    .requirements(of(
                                        requirement1101
                                    ))
                                    .build()

                            ))
                            .build()
                    )
                ).build()
            )
            .build();

        List<Phase> phases = of(
            Phase.ZERO,
            Phase.A,
            Phase.B,
            Phase.C,
            Phase.D,
            Phase.E,
            Phase.F
        );
        given(predicateMock.test("CDR", phases)).willReturn(true);
        given(predicateMock.test("PDR", phases)).willReturn(true);
        given(predicateMock.test("FAR", phases)).willReturn(false);


        // act
        Map<DRD, Set<String>> actual = provider.apply(catalog.getChapter("1").get(), phases);

        // assert
        assertThat(actual)
            .hasSize(2)
            .containsOnlyKeys(drd0101, drd0102);

        verify(predicateMock, times(3)).test(anyString(), eq(phases));
    }

    @Test
    void apply_OneDRDTwice_MapWithOneDRDEntryReturned() {
        // arrange
        DRD drd0101 = createDRD("01.01", "CDR");
        TailoringRequirement requirement0101 = TailoringRequirement.builder()
            .selected(true)
            .drds(of(drd0101))
            .build();

        TailoringRequirement requirement0102 = TailoringRequirement.builder()
            .selected(true)
            .drds(of(drd0101))
            .build();

        Catalog<TailoringRequirement> catalog = Catalog.<TailoringRequirement>builder()
            .toc(Chapter.<TailoringRequirement>builder()
                .name("/")
                .chapters(of(
                        Chapter.<TailoringRequirement>builder()
                            .number("1")
                            .requirements(of(
                                requirement0101,
                                requirement0102
                            ))
                            .build()
                    )
                ).build()
            )
            .build();

        List<Phase> phases = of(
            Phase.ZERO,
            Phase.A,
            Phase.B,
            Phase.C,
            Phase.D,
            Phase.E,
            Phase.F
        );
        given(predicateMock.test("CDR", phases)).willReturn(true);


        // act
        Map<DRD, Set<String>> actual = provider.apply(catalog.getChapter("1").get(), phases);

        // assert
        assertThat(actual)
            .hasSize(1)
            .containsOnlyKeys(drd0101);

        verify(predicateMock, times(2)).test(anyString(), eq(phases));
    }

    private DRD createDRD(String number, String deliveryDate) {
        return DRD.builder()
            .title("DRD " + number)
            .number(number)
            .deliveryDate(deliveryDate)
            .build();
    }

}
