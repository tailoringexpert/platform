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
import java.util.Collection;

@Data
@Builder
public class Tailoring implements Serializable {
    private static final long serialVersionUID = 8340200071078309053L;

    private String kennung;
    private String name;
    private SelektionsVektor selektionsVektor;
    private ScreeningSheet screeningSheet;
    private Collection<Phase> phasen;
    private Katalog<TailoringAnforderung> katalog;
    private TailoringStatus status;
    private Collection<Dokument> dokumente;
    private Collection<DokumentZeichnung> zeichnungen;
}
