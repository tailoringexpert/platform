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
package eu.tailoringexpert.screeningsheet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

import static java.util.List.of;

/**
 * Enum of all parameters provided/used on screeningsheet.<p>
 * Field name on screeningsheet have to match definied field names in Enum.
 * All entries are grouped by category.
 * <p>
 * Important:
 * Each field on screeningsheet must be added to this enum, otherwise field will not be extracted!
 *
 * @author Michael Bädorf
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum PlattformScreeningSheetParameter {

    ALLGEMEIN("Project",
        of("Project")),
    PHASE("Phase",
        of("0",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F"
        ));

    private final String category;

    private final Collection<String> parameter;

    public boolean hasMember(String parameter) {
        return this.parameter.contains(parameter);
    }

}
