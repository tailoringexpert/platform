package eu.tailoringexpert.tailoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRow.Tag;
import com.github.difflib.text.DiffRowGenerator;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.TailoringRequirement;
import lombok.Builder;
import lombok.Value;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ReequirementDiffTest {

    DiffRowGenerator generator;

    @BeforeEach
    void BeforeEach() {
        this.generator = DiffRowGenerator.create()
                .showInlineDiffs(true)
                .inlineDiffByWord(true)
                .oldTag(f -> "~")
                .newTag(f -> "**")
                .build();
    }

    @Test
    void doit() {
        // arrange
        List<DiffRow> rows = generator.generateDiffRows(
                Arrays.asList("This is a test sentence.", "This is the second line.", "And here is the finish."),
                Arrays.asList("This is a test for diffutils.", "This is the second line."));

        // act

        // assert
        log.debug("|original|new|");
        log.debug("|--------|---|");
        for (DiffRow row : rows) {
            log.debug("|" + row.getOldLine() + "|" + row.getNewLine() + "|");
        }

    }

    @Test
    void doit2() {
        // arrange
        Chapter<TailoringRequirement> base = Chapter.<TailoringRequirement>builder()
                .number("1")
                .requirements(List.of(
                        TailoringRequirement.builder()
                                .position("a")
                                .selected(Boolean.FALSE)
                                .text("Sample 1")
                                .build(),
                        TailoringRequirement.builder()
                                .position("b")
                                .selected(Boolean.FALSE)
                                .text("Sample 2")
                                .build()))
                .chapters(List.of(
                        Chapter.<TailoringRequirement>builder()
                                .number("1.1")
                                .requirements(List.of(
                                        TailoringRequirement.builder()
                                                .position("a")
                                                .selected(Boolean.FALSE)
                                                .text("Sample 1.1")
                                                .build()))
                                .build()))
                .build();

        Chapter<TailoringRequirement> compare = Chapter.<TailoringRequirement>builder()
                .number("1")
                .requirements(List.of(
                        TailoringRequirement.builder()
                                .position("a")
                                .selected(Boolean.TRUE)
                                .text("Sample 3")
                                .build(),
                        TailoringRequirement.builder()
                                .position("b")
                                .selected(Boolean.TRUE)
                                .text("Sample 4")
                                .build()))
                .chapters(List.of(
                        Chapter.<TailoringRequirement>builder()
                                .number("1.1")
                                .requirements(List.of(
                                        TailoringRequirement.builder()
                                                .position("a")
                                                .selected(Boolean.FALSE)
                                                .text("Sample 1.1 hanged")
                                                .build()))
                                .build()))
                .build();

        Map<String, List<TailoringRequirementDiff>> actual = new HashMap<>();

        // act
        accept(base, compare, actual);

        // assert
        assertThat(actual)
                .isNotEmpty()
                .hasSize(2);
        assertThat(actual.get("1"))
                .hasSize(2);

    }

    private void accept(Chapter<TailoringRequirement> base,
            Chapter<TailoringRequirement> compare,
            Map<String, List<TailoringRequirementDiff>> diffs) {

        compare.getRequirements().forEach(requirement -> accept(
                requirement,
                base.getRequirement(requirement.getPosition()).get(),
                diffs.computeIfAbsent(base.getNumber(), key -> new ArrayList<>())));

        Optional.ofNullable(compare.getChapters()).ifPresent(subChapters -> subChapters.forEach(subChapter -> accept(
                subChapter,
                base.getChapter(subChapter.getNumber()),
                diffs)));

        // () -> revised.getRequirements().forEach(requirement ->
        // accept(
        // requirement,
        // empty(),
        // replacements
        // )
        // )
        // );
        // base.ifPresentOrElse(baseChapter ->
        // revised.getRequirements().forEach(requirement ->
        // accept(
        // requirement,
        // baseChapter.getRequirement(requirement.getPosition())
        // )
        // ),
        // () -> revised.getRequirements().forEach(requirement ->
        // accept(
        // requirement,
        // empty()
        // )
        // )
        // );

        // ofNullable(revised.getChapters()).ifPresent(subChapters ->
        // subChapters.forEach(subChapter ->
        // accept(
        // subChapter,
        // base.map(baseSubChapter ->
        // baseSubChapter.getChapter(subChapter.getNumber())),
        // replacements
        // )
        // )
        // );
    }

    void accept(TailoringRequirement base, TailoringRequirement compare, List<TailoringRequirementDiff> diff) {
        List<DiffRow> diffs = generator.generateDiffRows(
                List.of(base.getText()),
                List.of(compare.getText()));

        if (diffs.isEmpty() || (!hasTextDiff(diffs) && !hasSelectionDiff(base, compare))) {
            return; // Optional.empty();
        }

        diff.add(TailoringRequirementDiff.builder()
                .base(TailoringRequirement.builder()
                        .position(base.getPosition())
                        .text(diffs.get(0).getOldLine())
                        .selected(base.getSelected())
                        .build())
                .other(TailoringRequirement.builder()
                        .position(compare.getPosition())
                        .text(diffs.get(0).getNewLine())
                        .selected(compare.getSelected())
                        .build())
                .build());

    }

    boolean hasSelectionDiff(TailoringRequirement base, TailoringRequirement compare) {
        return !base.getSelected().equals(compare.getSelected());
    }

    boolean hasTextDiff(List<DiffRow> diffs) {
        return !diffs.isEmpty() || !diffs.getFirst().getTag().equals(Tag.EQUAL);
    }

    @Builder
    @Value
    public static class TailoringRequirementDiff {
        TailoringRequirement base;
        TailoringRequirement other;

    }
}
