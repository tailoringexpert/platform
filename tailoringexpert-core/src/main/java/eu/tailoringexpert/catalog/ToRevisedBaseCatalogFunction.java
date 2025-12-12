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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.Map.entry;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

/**
 * Create a base catalog with marked changes.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
public class ToRevisedBaseCatalogFunction {

    @NonNull
    private TextDiff text;

    public static final String REPLACEMENT_ORIGINAL_VERSION = "$ORIGINAL_VERSION";
    public static final String REPLACEMENT_REVISED_VERSION = "$REVISED_VERSION";

    /**
     * Creates a basecatalog reflecting changes from the base to the compare version.
     * Please note that only modified and changed are handled.
     *
     * @param base         the base version base catalog
     * @param compare      the compare version  base catalog
     * @param replacements Map of texts to be replaced in base to "ignore" diff
     * @return
     */
    public Catalog<BaseRequirement> apply(Catalog<BaseRequirement> base,
                                          Catalog<BaseRequirement> compare,
                                          Map<String, String> replacements) {
        log.traceEntry(base::getVersion, compare::getVersion);

        Map<String, String> ignores = replacements.entrySet()
            .stream()
            .map(entry -> entry(
                entry.getKey().replace(REPLACEMENT_ORIGINAL_VERSION, base.getVersion()),
                entry.getValue().replace(REPLACEMENT_REVISED_VERSION, compare.getVersion())
            ))
            .collect(toMap(Entry::getKey, Entry::getValue));
        log.debug("Using following ignores replacements {}", ignores);

        Catalog<BaseRequirement> result = Catalog.<BaseRequirement>builder()
            .version(compare.getVersion())
            .toc(compare.getToc())
            .build();
        result.getToc().getChapters().forEach(chapter ->
            accept(chapter, base.getChapter(chapter.getNumber()), ignores)
        );

        log.traceExit();
        return result;
    }

    /**
     * Handles every subchapter of the revises chapter.
     *
     * @param revised      revised chapter evaluate
     * @param base         base chapter to compare to
     * @param replacements map of texts to be replaced in base to "ignore" diff
     */
    private void accept(Chapter<BaseRequirement> revised,
                        Optional<Chapter<BaseRequirement>> base,
                        Map<String, String> replacements) {
        base.ifPresentOrElse(baseChapter ->
                revised.getRequirements().forEach(requirement ->
                    accept(
                        requirement,
                        baseChapter.getRequirement(requirement.getPosition()),
                        replacements
                    )
                ),
            () -> revised.getRequirements().forEach(requirement ->
                accept(
                    requirement,
                    empty(),
                    replacements
                )
            )
        );

        ofNullable(revised.getChapters()).ifPresent(subChapters ->
            subChapters.forEach(subChapter ->
                accept(
                    subChapter,
                    base.map(baseSubChapter -> baseSubChapter.getChapter(subChapter.getNumber())),
                    replacements
                )
            )
        );
    }

    /**
     * Calculate diff of revised requirement and mark correspondingly.
     *
     * @param revised the revised requirement
     * @param base    the base requirement. Might not exists in case revised is a new requirement.
     */
    private void accept(BaseRequirement revised,
                        Optional<BaseRequirement> base,
                        Map<String, String> replacements) {
        text.diff(
            base.isPresent() ? base.get().getText() : null,
            revised.getText(),
            replacements
        ).ifPresent(revised::setText);
    }
}
