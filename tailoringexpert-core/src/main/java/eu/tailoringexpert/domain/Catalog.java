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
import java.util.Optional;
import java.util.stream.Stream;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catalog<T extends Requirement> implements Serializable {
    private static final long serialVersionUID = 2102054700551193540L;

    private String version;
    private Chapter<T> toc;

    public Stream<Chapter<T>> allChapters() {
        return toc.getChapters()
            .stream()
            .flatMap(Chapter::allChapters);
    }

    public Optional<Chapter<T>> getChapter(String number) {
        return allChapters()
            .filter(chapter -> number.equals(chapter.getNumber()))
            .findFirst();
    }
}