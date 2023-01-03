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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class ReferenceEntity implements Serializable {
    private static final long serialVersionUID = 5590162094478573160L;

    /**
     * Text where it references it to.
     */
    @Column(name = "REFERENCE")
    private String text;

    /**
     * State if referenced text is changed.
     */
    @Column(name = "REFERENCE_CHANGED")
    private Boolean changed;

    /**
     * Logo of reference.
     */
    @OneToOne
    @JoinColumn(name="REFERENCELOGO_ID", referencedColumnName = "LOGO_ID")
    private LogoEntity logo;


}
