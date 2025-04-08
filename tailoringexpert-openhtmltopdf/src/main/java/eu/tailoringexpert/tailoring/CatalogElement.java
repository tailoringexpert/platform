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
 * Requirement data element to be used for generating tailoring catalog document.
 *
 * @author Michael Bädorf
 */
@Value
@Builder
public class CatalogElement {

    /**
     * Text of element origin.
     */
    private String reference;

    private String referenceIssue;

    private String referenceReleaseDate;

    /**
     * Position to render element at.
     */
    private String position;

    /**
     * Chapter of element.
     */
    private String chapter;

    /**
     * Outputtext.
     */
    private String text;

    /**
     * +
     * State if element shall be shown.
     */
    private boolean applicable;

    /**
     * Level of element in chapter tree.
     */
    private int level;
}
