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

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;

import static java.util.Objects.nonNull;

@Value
@Builder
public class Datei implements Serializable {
    private static final long serialVersionUID = 8862689823397074307L;

    private String docId;
    private String type;
    private byte[] bytes;


    public long getLength() {
        return nonNull(bytes) ? bytes.length : 0;
    }

    public String getName() {
        return docId + "." + type;
    }


}
