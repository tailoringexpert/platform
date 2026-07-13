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

import java.util.Collection;

import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.SuperBuilder;

/**
 * Requirement data element to be used for generating base catalog document.
 *
 * @author Michael Bädorf
 */
@Value
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class BaseCatalogueElement extends CatalogueElement {

    /**
     * Identifiers this requirement shall be selected automatically.
     */
    @Singular
    private Collection<String> identifiers;

    /**
     * Phases the requirement belongs to.
     */
    @Singular
    private Collection<String> phases;

    private boolean added;

    private boolean changed;

    @Singular
    private Collection<DRDElement> drds;
}
