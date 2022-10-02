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
import java.time.ZonedDateTime;
import java.util.Collection;

@Data
public class TailoringRequirement extends Requirement implements Serializable {
    private static final long serialVersionUID = 3399819648313645377L;

    private Boolean selected;
    private ZonedDateTime selectionChanged;
    private ZonedDateTime textChanged;

    @Builder
    public TailoringRequirement(
        String text,
        String position,
        Collection<DRD> drds,
        Boolean selected,
        ZonedDateTime selectionChanged,
        ZonedDateTime textChanged,
        Reference reference) {
        super(text, position, reference, drds);
        this.selected = selected;
        this.selectionChanged = selectionChanged;
        this.textChanged = textChanged;
    }
}
