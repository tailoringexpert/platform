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

import lombok.Getter;

import java.util.Map;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("PMD.FieldDeclarationsShouldBeAtStartOfClass")
public enum Phase {
    ZERO("0"),
    A("A"),
    B("B"),
    C("C"),
    D("D"),
    E("E"),
    F("F");

    @Getter
    private String value;

    Phase(String value) {
        this.value = value;
    }

    private static Map<String, Phase> reverseLookup = stream(values()).collect(toMap(e -> e.value, Function.identity()));

    public static Phase fromString(final String id) {
        return reverseLookup.get(id);
    }
}
