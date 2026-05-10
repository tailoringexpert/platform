/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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
import lombok.experimental.SuperBuilder;

/**
 * Data element to be used for generating catalog documents.
 *
 * @author Michael Bädorf
 */
@Getter
@SuperBuilder
public abstract class CatalogElement {

    /**
     * Requirement origin.
     */
    private String reference;

    /**
     * Logo of requirement originator.
     */
    private String logo;

    /**
     * Position (in chapter) of requirement.
     */

    private String position;

    /**
     *
     */
    private String chapter;

    /**
     * Text of the requirement.
     */
    private String text;

    /**
     * Level of element in chapter tree.
     */
    private int level;

}
