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

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.difflib.text.DiffRow.Tag.EQUAL;
import static java.util.Collections.emptyList;
import static java.util.List.of;
import static java.util.Objects.nonNull;

@AllArgsConstructor
public class DifflibTextDiff implements TextDiff {

    @NonNull
    private DiffRowGenerator diffRowGenerator;

    @Override
    public Optional<String> diff(String base, String compare, Map<String, String> replacements) {
        List<DiffRow> diffs = diffRowGenerator.generateDiffRows(
            nonNull(base) ? of(applyReplacements(base, replacements)) : emptyList(),
            of(compare)
        );
        return diffs.isEmpty() || diffs.getFirst().getTag() == EQUAL ?
            Optional.empty() :
            Optional.ofNullable(diffs.getFirst().getOldLine());
    }

    /**
     * Applies text replacements to a string using the provided map.
     *
     * @param text         the input text
     * @param replacements map of key-value pairs for replacement
     * @return the text with replacements applied
     */
    private String applyReplacements(String text, Map<String, String> replacements) {
        String result = text;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
