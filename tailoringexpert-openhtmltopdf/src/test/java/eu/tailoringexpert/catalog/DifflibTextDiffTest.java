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
package eu.tailoringexpert.catalog;

import com.github.difflib.text.DiffRowGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static eu.tailoringexpert.catalog.ToRevisedBaseCatalogFunction.REPLACEMENT_ORIGINAL_VERSION;
import static eu.tailoringexpert.catalog.ToRevisedBaseCatalogFunction.REPLACEMENT_REVISED_VERSION;
import static java.util.Map.of;
import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

class DifflibTextDiffTest {

    TextDiff text;

    @BeforeEach
    void beforeEach() {
        this.text = new DifflibTextDiff(DiffRowGenerator.create()
            .reportLinesUnchanged(false)
            .showInlineDiffs(true)
            .mergeOriginalRevised(true)
            .inlineDiffByWord(true)
            .ignoreWhiteSpaces(true)
            .lineNormalizer(identity())
            .oldTag((tag, f) -> f ? "<span class='old'>" : "</span>")
            .newTag((tag, f) -> f ? "<span class='new'>" : "</span>")
            .build()
        );
    }

    @Test
    void diff_TextSame_EmptyReturned() {
        // arrange
        String base = "This is the requirement of catalog 8.2.2";
        String revised = "This is the requirement of catalog 8.2.2";
        Map<String, String> replacements = of(
            "/assets/" + REPLACEMENT_ORIGINAL_VERSION, "/assets/" + REPLACEMENT_REVISED_VERSION
        );

        // act
        Optional<String> actual = text.diff(base, revised, replacements);

        // assert
        assertThat(actual)
            .isEmpty();
    }

    @Test
    void diff_ChangedTextNoIgnoredReplacement_ChangeReturned() {
        // arrange
        String base = "This is the requirement of catalog 8.2.2";
        String revised = "This is the requirement of catalog 9.0.0";
        Map<String, String> replacements = of(
            "/assets/8.2.2", "/assets/9.0.0"
        );

        // act
        Optional<String> actual = text.diff(base, revised, replacements);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .hasValue("This is the requirement of catalog <span class='old'>8</span><span class='new'>9</span>.<span class='old'>2</span><span class='new'>0</span>.<span class='old'>2</span><span class='new'>0</span>");
    }

    @Test
    void diff_ChangedTextIgnoredReplacement_EmptyReturned() {
        // arrange
        String base = "This is the requirement of catalog 8.2.2";
        String revised = "This is the requirement of catalog 9.0.0";
        Map<String, String> replacements = of(
            "8.2.2", "9.0.0"
        );

        // act
        Optional<String> actual = text.diff(base, revised, replacements);

        // assert
        assertThat(actual)
            .isEmpty();
    }

    @Test
    void diff_TextEqual_EmptyReturned() {
        // arrange
        String base = "This is the requirement of catalog 8.2.2";
        String revised = "This is the requirement of catalog 8.2.2";
        Map<String, String> replacements = of();

        // act
        Optional<String> actual = text.diff(base, revised, replacements);

        // assert
        assertThat(actual)
            .isEmpty();
    }

    @Test
    void diff_BaseNull_ChangedReturned() {
        // arrange
        String base = null;
        String revised = "This is the requirement of catalog 9.0.0";
        Map<String, String> replacements = of(
            "8.2.2", "9.0.0"
        );

        // act
        Optional<String> actual = text.diff(base, revised, replacements);

        // assert
        assertThat(actual)
            .isNotEmpty()
            .hasValue("<span class='new'>This is the requirement of catalog 9.0.0</span>");
    }

    @Test
    void diff_DiffRowEmpty_EmptyReturned() {
        // arrange
        DiffRowGenerator rowGeneratorMock = Mockito.mock(DiffRowGenerator.class);
        TextDiff difflib = new DifflibTextDiff(rowGeneratorMock);

        String base = "This is the requirement of catalog 9.0.0";
        String revised = "This is the requirement of catalog 9.0.0";
        Map<String, String> replacements = of();

        given(rowGeneratorMock.generateDiffRows(List.of(anyString()), List.of(anyString())))
            .willReturn(List.of());

        // act
        Optional<String> actual = difflib.diff(base, revised, replacements);

        // assert
        assertThat(actual)
            .isEmpty();
    }


}
