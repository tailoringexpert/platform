package eu.tailoringexpert.tailoring;

import static java.util.Optional.empty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRow.Tag;
import com.github.difflib.text.DiffRowGenerator;

import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TailoringRequirmentTextDiffProvider
        implements BiFunction<TailoringRequirement, TailoringRequirement, Optional<TailoringRequirementDiff>> {

    @NonNull
    DiffRowGenerator generator;

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @Override
    public Optional<TailoringRequirementDiff> apply(TailoringRequirement base, TailoringRequirement compare) {
        List<DiffRow> diffs = generator.generateDiffRows(
                List.of(base.getText()),
                List.of(compare.getText()));

        if (!hasTextDiff(diffs) && !hasSelectionDiff(base, compare)) {
            return empty();
        }

        return Optional.of(TailoringRequirementDiff.builder()
                .base(TailoringRequirement.builder()
                        .position(base.getPosition())
                        .text(templateEngine.toXHTML(diffs.get(0).getOldLine(), Map.of()))
                        .selected(base.getSelected())
                        .build())
                .other(TailoringRequirement.builder()
                        .position(compare.getPosition())
                        .text(templateEngine.toXHTML(diffs.get(0).getNewLine(), Map.of()))
                        .selected(compare.getSelected())
                        .build())
                .build());
    }

    private boolean hasSelectionDiff(TailoringRequirement base, TailoringRequirement compare) {
        return !base.getSelected().equals(compare.getSelected());
    }

    private boolean hasTextDiff(List<DiffRow> diffs) {
        return !diffs.getFirst().getTag().equals(Tag.EQUAL);
    }
}
