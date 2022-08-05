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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import java.io.Serializable;

import static javax.persistence.EnumType.STRING;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class DokumentZeichnungEntity implements Serializable {
    private static final long serialVersionUID = 6380646019782563133L;

    @Column(name = "BEREICH")
    private String bereich;

    @Column(name = "UNTERZEICHNER")
    private String unterzeichner;

    @Column(name = "STATUS")
    @Enumerated(STRING)
    private DokumentZeichnungStatus status;

    @Column(name = "ANWENDBAR")
    private Boolean anwendbar;

    private int position;
}
