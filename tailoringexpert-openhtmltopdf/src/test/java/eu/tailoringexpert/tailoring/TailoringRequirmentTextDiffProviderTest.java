package eu.tailoringexpert.tailoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.github.difflib.text.DiffRowGenerator;

import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;

class TailoringRequirmentTextDiffProviderTest {

    HTMLTemplateEngine templateEngineMock;
    TailoringRequirmentTextDiffProvider provider;

    @BeforeEach
    void beforeEach() {

        this.templateEngineMock = Mockito.mock(HTMLTemplateEngine.class);

        DiffRowGenerator generator = DiffRowGenerator.create()
                .reportLinesUnchanged(false)
                .showInlineDiffs(true)
                .mergeOriginalRevised(false)
                .inlineDiffByWord(true)
                .ignoreWhiteSpaces(true)
                .lineNormalizer(Function.identity())
                // .oldTag((tag, f) -> f ? "<span class='requirement-old'>" : "</span>")
                .newTag((tag, f) -> f ? "<span class='requirement-new'>" : "</span>")
                .build();

        this.provider = new TailoringRequirmentTextDiffProvider(generator, templateEngineMock);
    }

    @Test
    void apply_NoDiff_EmptyReturned() {
        // arrange
        given(templateEngineMock.toXHTML(anyString(), anyMap()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // act
        Optional<TailoringRequirementDiff> actual = provider.apply(
                TailoringRequirement.builder()
                        .text("No change test")
                        .selected(true)
                        .build(),
                TailoringRequirement.builder()
                        .text("No change test")
                        .selected(true)
                        .build());

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void apply_DifferentText_TailoringRequirementDiffReturned() {
        // arrange
        given(templateEngineMock.toXHTML(anyString(), anyMap()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // act
        Optional<TailoringRequirementDiff> actual = provider.apply(
                TailoringRequirement.builder()
                        .text("No change test")
                        .selected(true)
                        .build(),
                TailoringRequirement.builder()
                        .text("change test")
                        .selected(true)
                        .build());

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getOther().getText()).isEqualTo("change test");
    }

    @Test
    void apply_DifferentSelection_TailoringRequirementDiffReturned() {
        // arrange
        given(templateEngineMock.toXHTML(anyString(), anyMap()))
                .willAnswer(invocation -> invocation.getArgument(0));

        // act
        Optional<TailoringRequirementDiff> actual = provider.apply(
                TailoringRequirement.builder()
                        .text("No change test")
                        .selected(true)
                        .build(),
                TailoringRequirement.builder()
                        .text("No change test")
                        .selected(false)
                        .build());

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getOther().getSelected()).isEqualTo(false);
    }

}
