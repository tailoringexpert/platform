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
import lombok.Value;

import java.util.Collection;

/**
 * Data element to be used for generating DRD document.
 *
 * @author Michael Bädorf
 */
@Value
@Builder
public class DRDElement {

    /**
     * Number of the DRD.
     */
    private String number;

    /**
     * Title of the DRD.
     */
    private String title;

    private String subtitle;

    /**
     * Definition when to deliver the document.
     */
    private String deliveryDate;

    /**
     * What to do.
     */
    private String action;

    /**
     * Requirements the DRD in referenced in.
     */
    private Collection<String> requirements;
}
