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
public class Kapitel<T extends Anforderung> implements Serializable {
    private static final long serialVersionUID = -3448078519876131441L;

    private String name;
    private int position;
    private String nummer;

    @SuppressWarnings({"java:S1700", "PMD.AvoidFieldNameMatchingTypeName"})
    private List<Kapitel<T>> kapitel;
    private List<T> anforderungen;


    public Kapitel<T> getKapitel(String kapitel) {
        return allKapitel()
            .filter(gruppe -> kapitel.equals(gruppe.getNummer()))
            .findFirst()
            .orElse(null);
    }


    public Stream<Kapitel<T>> allKapitel() {
        return Stream.concat(
            of(this),
            nonNull(kapitel) ? kapitel.stream().flatMap(Kapitel::allKapitel) : Stream.empty());

    }

    public Stream<T> allAnforderungen() {
        return allKapitel()
            .flatMap(h -> nonNull(h.anforderungen) ? h.anforderungen.stream() : Stream.empty());
    }

    public Optional<T> getAnforderung(String position) {
        return anforderungen
            .stream()
            .filter(anforderung -> anforderung.getPosition().equals(position))
            .findFirst();
    }

    public OptionalInt indexOfAnforderung(String position) {
        return IntStream.range(0, anforderungen.size())
            .filter(i -> position.equals(anforderungen.get(i).getPosition()))
            .findFirst();
    }

}
