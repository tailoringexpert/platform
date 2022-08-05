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
package de.baedorf.tailoringexpert.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Data
@Builder
public class ScreeningSheet implements Serializable {
    private static final long serialVersionUID = 3465063609780635739L;

    public static final String KUERZEL = "kuerzel";
    public static final String LANGNAME = "langname";
    public static final String KURZNAME = "kurzname";

    private byte[] data;
    private List<ScreeningSheetParameter> parameters;
    private SelektionsVektor selektionsVektor;

    public String getKuerzel() {
        Optional<ScreeningSheetParameter> result = parameters.stream()
            .filter(parameter -> KUERZEL.equalsIgnoreCase(parameter.getBezeichnung()))
            .findFirst();

        return result.isPresent() ? (String) result.get().getWert() : null;
    }
}
