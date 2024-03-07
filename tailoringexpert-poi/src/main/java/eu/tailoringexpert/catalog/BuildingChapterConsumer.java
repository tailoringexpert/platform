/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2024 Michael Bädorf and others
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
import eu.tailoringexpert.domain.Chapter;

import java.util.Comparator;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

/**
 * Builds hierarchy of chapters.
 *
 * @author Michael Bädorf
 */
public class BuildingChapterConsumer implements BiConsumer<Chapter<BaseRequirement>, Map<String, Chapter<BaseRequirement>>> {
    /**
     * Builds the root hierarchy.<p>
     * All chapters provided in {@code chapters} parameter will be added in accordance to their hierachy to the
     * provided root parameter.
     *
     * @param root     Chapter to add direct childs to
     * @param chapters Map of all chapters identifies by their number
     */
    @Override
    public void accept(Chapter<BaseRequirement> root, Map<String, Chapter<BaseRequirement>> chapters) {
        Comparator<Chapter<BaseRequirement>> comparator = comparing(Chapter::getPosition);
        chapters.keySet()
            .forEach(number -> {
                int index = number.lastIndexOf(".");
                if (index == -1) {
                    Chapter<BaseRequirement> bc = chapters.get(number);
                    sort(bc.getChapters(), comparator);
                    root.getChapters().add(bc);
                } else {
                    Chapter<BaseRequirement> bc = chapters.get(number);
                    sort(bc.getChapters(), comparator);
                    String parent = number.substring(0, index);
                    chapters.get(parent).getChapters().add(bc);
                    sort(chapters.get(parent).getChapters(), comparator);
                }
            });
        sort(root.getChapters(), comparator);

    }
}
