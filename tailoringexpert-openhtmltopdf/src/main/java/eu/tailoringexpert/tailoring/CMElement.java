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
package eu.tailoringexpert.tailoring;

import lombok.Builder;
import lombok.Value;

/**
 * Data element to be used for generating compliance matrix document.
 *
 * @author Michael Bädorf
 */
@Value
@Builder
public class CMElement {

    /**
     * Level in chapter.
     */
    private int level;

    /**
     * Number of chapter.
     */
    private String number;

    /**
     * Name of chapter.
     */
    private String name;
}
