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

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Data
@Builder
public class ScreeningSheet implements Serializable {
    private static final long serialVersionUID = 3465063609780635739L;

    /**
     * Name of the mandatory project parameter.
     */
    public static final String PARAMETER_PROJECT = "project";

    /**
     * Name of the mandatory phases parameter.
     */
    public static final String PARAMETER_PHASE = "phase";

    /**
     * Raw data of the file.
     */
    private byte[] data;

    /**
     * Name of the project screeningsheet belongs to.
     */
    private String project;

    /**
     * Parameters extracted of screeningsheet.
     */
    private List<ScreeningSheetParameter> parameters;

    /**
     * Calculated selectionvector wit screeningsheet parameters.
     */
    private SelectionVector selectionVector;

    /**
     * Phases of tailoring.
     */
    private Collection<Phase> phases;

}
