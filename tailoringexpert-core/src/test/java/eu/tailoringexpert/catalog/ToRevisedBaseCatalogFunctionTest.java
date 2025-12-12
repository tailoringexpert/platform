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

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
class ToRevisedBaseCatalogFunctionTest {

    TextDiff textMock;
    ToRevisedBaseCatalogFunction toRevisedFunction;


    @BeforeEach
    void beforeEach() {
        this.textMock = mock(TextDiff.class);
        this.toRevisedFunction = new ToRevisedBaseCatalogFunction(this.textMock);
    }

    @Test
    void apply_IgnoredTextChange_NoChangeInRevisedRequirement() {
        Catalog<BaseRequirement> base = Catalog.<BaseRequirement>builder()
            .version("8.2.2")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(List.of(
                    Chapter.<BaseRequirement>builder()
                        .number("1")
                        .requirements(asList(
                            BaseRequirement.builder()
                                .position("a")
                                .text("This is the requirement of catalog 8.2.2")
                                .build())
                        )
                        .build())
                )
                .build()
            )
            .build();

        Catalog<BaseRequirement> compare = Catalog.<BaseRequirement>builder()
            .version("9.0.0")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(List.of(
                    Chapter.<BaseRequirement>builder()
                        .number("1")
                        .requirements(asList(
                            BaseRequirement.builder()
                                .position("a")
                                .text("This is the requirement of catalog 9.0.0")
                                .build())
                        )
                        .build())
                )
                .build()
            )
            .build();

        Map<String, String> replacements = of(
            "8.2.2", "9.0.0"
        );

        given(textMock.diff("This is the requirement of catalog 8.2.2", "This is the requirement of catalog 9.0.0", replacements))
            .willReturn(Optional.of("This is the requirement of catalog 9.0.0"));

        // act
        Catalog<BaseRequirement> actual = toRevisedFunction.apply(base, compare, replacements);

        // assert
        assertThat(actual.getToc().getChapter("1").getRequirement("a").get().getText())
            .isEqualTo("This is the requirement of catalog 9.0.0");
    }

    @Test
    void apply_TextChangesNoReplacement_RequirementTextUpdated() {
        Catalog<BaseRequirement> original = Catalog.<BaseRequirement>builder()
            .version("8.2.2")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(List.of(
                    Chapter.<BaseRequirement>builder()
                        .number("1")
                        .requirements(asList(
                            BaseRequirement.builder()
                                .position("a")
                                .text("This is the requirement of catalog 8.2.2")
                                .build())
                        )
                        .build())
                )
                .build()
            )
            .build();

        Catalog<BaseRequirement> revised = Catalog.<BaseRequirement>builder()
            .version("9.0.0")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(List.of(
                    Chapter.<BaseRequirement>builder()
                        .number("1")
                        .requirements(asList(
                            BaseRequirement.builder()
                                .position("a")
                                .text("This is the requirement of catalog 9.0.0")
                                .build())
                        )
                        .build())
                )
                .build()
            )
            .build();

        Map<String, String> replacements = of(
            "/assets/8.2.2", "/assets/9.0.0"
        );

        given(textMock.diff("This is the requirement of catalog 8.2.2", "This is the requirement of catalog 9.0.0", replacements))
            .willReturn(Optional.of("This is the requirement of catalog <span class='old'>8</span><span class='new'>9</span>.<span class='old'>2</span><span class='new'>0</span>.<span class='old'>2</span><span class='new'>0</span>"));

        // act
        Catalog<BaseRequirement> actual = toRevisedFunction.apply(original, revised, replacements);

        // assert
        assertThat(actual.getToc().getChapter("1").getRequirement("a").get().getText())
            .isEqualTo("This is the requirement of catalog <span class='old'>8</span><span class='new'>9</span>.<span class='old'>2</span><span class='new'>0</span>.<span class='old'>2</span><span class='new'>0</span>");
    }

    @Test
    void apply_NewRequirementExistingBaseChapter_AllRequirementsEvaluated() {
        Catalog<BaseRequirement> original = Catalog.<BaseRequirement>builder()
            .version("8.2.2")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(List.of(
                    Chapter.<BaseRequirement>builder()
                        .number("1")
                        .requirements(asList(
                            BaseRequirement.builder()
                                .position("a")
                                .text("This is the requirement of catalog 8.2.2")
                                .build())
                        )
                        .build())
                )
                .build()
            )
            .build();

        Catalog<BaseRequirement> revised = Catalog.<BaseRequirement>builder()
            .version("9.0.0")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(List.of(
                    Chapter.<BaseRequirement>builder()
                        .number("1")
                        .requirements(asList(
                            BaseRequirement.builder()
                                .position("a")
                                .text("This is the requirement of catalog 9.0.0")
                                .build(),
                            BaseRequirement.builder()
                                .position("b")
                                .text("New requirement of catalog 9.0.0")
                                .build())
                        )
                        .build())
                )
                .build()
            )
            .build();

        Map<String, String> replacements = of(
            "8.2.2", "9.0.0"
        );

        given(textMock.diff(null, "New requirement of catalog 9.0.0", replacements))
            .willReturn(Optional.of("<span class='new'>New requirement of catalog 9.0.0</span>"));

        // act
        Catalog<BaseRequirement> actual = toRevisedFunction.apply(original, revised, replacements);

        // assert
        assertThat(actual.getToc().getChapter("1").getRequirement("b").get().getText())
            .isEqualTo("<span class='new'>New requirement of catalog 9.0.0</span>");
        verify(textMock, times(1))
            .diff(
                "This is the requirement of catalog 8.2.2",
                "This is the requirement of catalog 9.0.0",
                replacements
            );
    }


    @Test
    void apply_NewRequirementNotExistingBaseChapter_AllRevisedRequirementsEvaluated() {

        Catalog<BaseRequirement> original = Catalog.<BaseRequirement>builder()
            .version("8.2.2")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(List.of(
                    Chapter.<BaseRequirement>builder()
                        .number("1")
                        .requirements(asList(
                            BaseRequirement.builder()
                                .position("a")
                                .text("This is the requirement of catalog 8.2.2")
                                .build())
                        )
                        .build())
                )
                .build()
            )
            .build();

        Catalog<BaseRequirement> revised = Catalog.<BaseRequirement>builder()
            .version("9.0.0")
            .toc(Chapter.<BaseRequirement>builder()
                .name("/")
                .chapters(List.of(
                        Chapter.<BaseRequirement>builder()
                            .number("1")
                            .requirements(asList(
                                BaseRequirement.builder()
                                    .position("a")
                                    .text("This is the requirement of catalog 9.0.0")
                                    .build()
                            ))
                            .chapters(asList(
                                    Chapter.<BaseRequirement>builder()
                                        .number("1.1")
                                        .requirements(asList(
                                            BaseRequirement.builder()
                                                .position("a")
                                                .text("New requirement of catalog 9.0.0 in subchapter 1.1")
                                                .build())
                                        )
                                        .build()
                                )
                            )
                            .build()
                    )
                )
                .build()
            )
            .build();


        Map<String, String> replacements = of(
            "8.2.2", "9.0.0"
        );

        given(textMock.diff(null, "New requirement of catalog 9.0.0 in subchapter 1.1", replacements))
            .willReturn(Optional.of("<span class='new'>New requirement of catalog 9.0.0 in subchapter 1.1</span>"));

        // act
        Catalog<BaseRequirement> actual = toRevisedFunction.apply(original, revised, replacements);

        // assert
        assertThat(actual.getToc().getChapter("1.1").getRequirement("a").get().getText())
            .isEqualTo("<span class='new'>New requirement of catalog 9.0.0 in subchapter 1.1</span>");

        verify(textMock, times(1))
            .diff(
                "This is the requirement of catalog 8.2.2",
                "This is the requirement of catalog 9.0.0",
                replacements
            );
        verify(textMock, times(1))
            .diff(
                null,
                "New requirement of catalog 9.0.0 in subchapter 1.1",
                replacements
            );
    }

}
