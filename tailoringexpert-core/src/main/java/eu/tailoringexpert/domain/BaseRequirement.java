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
import lombok.Singular;

import java.io.Serializable;
import java.util.Collection;

@Data
public class BaseRequirement extends Requirement implements Serializable {
    private static final long serialVersionUID = 1520118462633417279L;

    @Singular("identifier")
    private Collection<Identifier> identifiers;

    @Singular("phase")
    private Collection<Phase> phases;

    @Builder
    public BaseRequirement(
        String text,
        String position,
        Collection<DRD> drds,
        Collection<Phase> phases,
        Collection<Identifier> identifiers,
        Reference reference) {
        super(text, position, reference, drds);
        this.phases = phases;
        this.identifiers = identifiers;
    }
}
