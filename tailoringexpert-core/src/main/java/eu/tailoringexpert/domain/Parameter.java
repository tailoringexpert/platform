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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Parameter implements Serializable {
    private static final long serialVersionUID = 8412257758609369625L;

    /**
     * Category/group parameter belongs to.
     */
    private String category;

    /**
     * (Technical)Name of the parameter.
     */
    private String name;

    /**
     * Label to show for parameter.
     */
    private String label;

    /**
     * Type of the value.
     */
    private DatenType parameterType;

    /**
     * Value of the parameter.
     */
    private Serializable value;

    /**
     * Ordering position.
     */
    private int position;
}
