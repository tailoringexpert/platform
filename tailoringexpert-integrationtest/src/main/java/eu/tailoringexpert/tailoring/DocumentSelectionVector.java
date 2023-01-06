/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2023 Michael Bädorf and others
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

import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static java.util.Map.entry;

@Value
@Builder
public class DocumentSelectionVector {

    private static final Map<String, String> selectionVectorParameter = Map.ofEntries(
        entry("A", "Product Assurance"),
        entry("B", "Ground Segment"),
        entry("C", "Cyber- and Datasecurity"),
        entry("E", "EEE Components"),
        entry("F", "Planetary Protection"),
        entry("J", "Space Debris"),
        entry("M", "Maintainability"),
        entry("P", "PMMP"),
        entry("Q", "Quality Assurance"),
        entry("R", "Reliabilty"),
        entry("S", "Safety"),
        entry("W", "Software")
    );

    String type;
    Integer level;

    public String getName() {
        return selectionVectorParameter.getOrDefault(type, type);
    }

}
