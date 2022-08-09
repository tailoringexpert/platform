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
public class TailoringAnforderung extends Anforderung implements Serializable {
    private static final long serialVersionUID = 3399819648313645377L;

    private Boolean ausgewaehlt;
    private ZonedDateTime ausgewaehltGeaendert;
    private ZonedDateTime textGeaendert;

    @Builder
    public TailoringAnforderung(
        String text,
        String position,
        Collection<DRD> drds,
        Boolean ausgewaehlt,
        ZonedDateTime ausgewaehltGeaendert,
        ZonedDateTime textGeaendert,
        Referenz referenz) {
        super(text, position, referenz, drds);
        this.ausgewaehlt = ausgewaehlt;
        this.ausgewaehltGeaendert = ausgewaehltGeaendert;
        this.textGeaendert = textGeaendert;
    }

}
