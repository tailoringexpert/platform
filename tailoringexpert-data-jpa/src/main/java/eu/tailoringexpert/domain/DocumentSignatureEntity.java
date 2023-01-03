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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Enumerated;
import java.io.Serializable;

import static jakarta.persistence.EnumType.STRING;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class DocumentSignatureEntity implements Serializable {
    private static final long serialVersionUID = 6380646019782563133L;

    /**
     * Name of the faculty.
     */
    @Column(name = "FACULTY")
    private String faculty;

    /**
     * Name of the signee.
     */
    @Column(name = "SIGNEE")
    private String signee;

    /**
     * State of signature.
     */
    @Column(name = "STATE")
    @Enumerated(STRING)
    private DocumentSignatureState state;

    /**
     * State if signature shall be used/applicable.
     */
    @Column(name = "APPLICABLE")
    private Boolean applicable;

    /**
     * Position in signature list.
     */
    @Column(name = "POSITION")
    private int position;
}
