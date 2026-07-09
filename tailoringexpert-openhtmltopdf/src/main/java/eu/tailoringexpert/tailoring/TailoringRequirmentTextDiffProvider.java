/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import static java.util.Objects.isNull;
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
public class TailoringRequirmentTextDiffProvider implements
        BiFunction<TailoringRequirement, TailoringRequirement, Optional<TailoringRequirementDiff>> {

    @NonNull
    private DiffRowGenerator generator;

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @Override
    public Optional<TailoringRequirementDiff> apply(TailoringRequirement base, TailoringRequirement compare) {
        if (isNull(compare)) {
            return apply(base);
        }

        List<DiffRow> diffs = generator.generateDiffRows(
                List.of(base.getText()),
                List.of(compare.getText()));

        if (!hasTextDiff(diffs) && !hasSelectionDiff(base, compare)) {
            return empty();
        }

        DiffRow firstDiff = diffs.getFirst();
        return Optional.of(TailoringRequirementDiff.builder()
                .base(buildRequirement(base, firstDiff.getOldLine()))
                .other(buildRequirement(compare, firstDiff.getNewLine()))
                .build());
    }

    private Optional<TailoringRequirementDiff> apply(TailoringRequirement requirement) {
        return Optional.of(TailoringRequirementDiff.builder()
                .base(buildRequirement(requirement, requirement.getText()))
                .other(TailoringRequirement.builder().selected(Boolean.FALSE).build())
                .build());
    }

    private boolean hasSelectionDiff(TailoringRequirement base, TailoringRequirement compare) {
        return !base.getSelected().equals(compare.getSelected());
    }

    private boolean hasTextDiff(List<DiffRow> diffs) {
        return !diffs.getFirst().getTag().equals(Tag.EQUAL);
    }

    private TailoringRequirement buildRequirement(TailoringRequirement source, String rawText) {
        return TailoringRequirement.builder()
                .position(source.getPosition())
                .text(templateEngine.toXHTML(rawText, Map.of()))
                .selected(source.getSelected())
                .build();
    }
}
