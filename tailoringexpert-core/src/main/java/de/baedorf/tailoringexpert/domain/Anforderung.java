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

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

@Data
public abstract class Anforderung implements Serializable {
    private static final long serialVersionUID = 7854311335886291270L;

    private String text;
    private String position;
    private Referenz referenz;
    private Collection<DRD> drds;


    protected Anforderung(String text, String position, Referenz referenz, Collection<DRD> drds) {
        this.text = text;
        this.position = position;
        this.referenz = referenz;
        this.drds = drds;
    }


}
