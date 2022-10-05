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

import java.io.Serializable;

import static java.util.Objects.nonNull;

@Value
@Builder
public class File implements Serializable {
    private static final long serialVersionUID = 8862689823397074307L;

    /**
     * Name of the file
     */
    String name;

    /**
     * Content of the file.
     */
    byte[] data;

    /**
     * Checksum/Hash of the file.
     */
    String hash;


    /**
     * Returns length of file.
     *
     * @return length of file
     */
    public long getLength() {
        return nonNull(data) ? data.length : 0;
    }

    /**
     * Get type/file extension of file.
     *
     * @return test after last .
     */
    public String getType() {
        int index = name.lastIndexOf('.');
        return index > -1 ? name.substring(index + 1) : null;
    }

}
