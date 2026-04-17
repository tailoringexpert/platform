/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Meta/File information of a matrix file.
 * 
 * @author Michael Bädorf
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrixFileMeta {

    /**
     * Filename.
     */
    private String name;

    /**
     * Description of intended mission type.
     */
    private String description;

    /**
     * Version of basecatalogue used to create matrix.
     */
    private String catalogueVersion;

    /**
     * Hash of matrix file.
     */
    private String hash;

    /**
     * Creation timestamp of matrix.
     */
    private ZonedDateTime creationTimestamp;
}
