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
package eu.tailoringexpert.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.stream.Stream.of;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Chapter<T extends Requirement> implements Serializable {
    private static final long serialVersionUID = -3448078519876131441L;

    /**
     * Name of chapter.
     */
    private String name;

    /**
     * Position in chapter list.
     */
    private int position;

    /**
     * (Full) Number of chapter.
     */
    private String number;

    /**
     * List of subchapters.
     */
    private List<Chapter<T>> chapters;

    /**
     * Requirements defined in chapter.
     */
    private List<T> requirements;

    /**
     * Get a chapter identified by chapter number.
     *
     * @param number number of chapter to get
     * @return Chapter if exists
     */
    public Chapter<T> getChapter(String number) {
        return allChapters()
            .filter(chapter -> number.equals(chapter.getNumber()))
            .findFirst()
            .orElse(null);
    }

    /**
     * All chapters including subchapters.
     *
     * @return Stream of complete/all chapters of catalog.
     */
    public Stream<Chapter<T>> allChapters() {
        return Stream.concat(
            of(this),
            nonNull(chapters) ? chapters.stream().flatMap(Chapter::allChapters) : Stream.empty());

    }

    /**
     * All requirements of chapter and all subchapters.
     *
     * @return Stream of all requirements
     */
    public Stream<T> allRequirements() {
        return allChapters()
            .flatMap(chapter -> nonNull(chapter.requirements) ? chapter.requirements.stream() : Stream.empty());
    }

    /**
     * Get requirements of chapter.
     *
     * @param position position in chapter of requirement to get
     * @return requirement if exists, otherwiese empty
     */
    public Optional<T> getRequirement(String position) {
        return requirements
            .stream()
            .filter(requirement -> position.equals(requirement.getPosition()))
            .findFirst();
    }

    /**
     * Get the index of the requirement at requested position in chapter.
     *
     * @param position position of requirement to get index of
     * @return index of requirement in requirement list
     */
    public OptionalInt indexOfRequirement(String position) {
        return IntStream.range(0, requirements.size())
            .filter(i -> position.equals(requirements.get(i).getPosition()))
            .findFirst();
    }

}
