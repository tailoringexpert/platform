/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2025 Michael Bädorf and others
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
package eu.tailoringexpert.domain;

import eu.tailoringexpert.tailoring.RequirementSelectedPredicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;

class ApplicableDocumentProviderTest {

    ApplicableDocumentProvider<TailoringRequirement> provider;


    @BeforeEach
    void beforeEach() {
        this.provider = new ApplicableDocumentProvider<>(
            new RequirementSelectedPredicate(),
            new DocumentNumberComparator());
    }


    @Test
    void doit() {
        // arrange
        Document q80 = Document.builder()
            .number("bh")
            .title("ECSS-Q-ST-80")
            .issue("C")
            .revision("Rev.1")
            .build();

        Document e40 = Document.builder()
            .number("m")
            .title("ECSS-E-ST-40")
            .issue("C")
            .revision("Rev.1")
            .build();

        TailoringRequirement requirement0101 = TailoringRequirement.builder()
            .selected(true)
            .applicableDocuments(Arrays.asList(
                q80
            ))
            .build();

        TailoringRequirement requirement0102 = TailoringRequirement.builder()
            .selected(true)
            .applicableDocuments(Arrays.asList(
                q80
            ))
            .build();

        TailoringRequirement requirement1101 = TailoringRequirement.builder()
            .selected(true)
            .applicableDocuments(Arrays.asList(
                q80,
                e40
            ))
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


        // act
        Collection<Document> actual = provider.apply(catalog);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .hasSize(2)
            .containsExactlyElementsOf(of(e40, q80));
    }

}
