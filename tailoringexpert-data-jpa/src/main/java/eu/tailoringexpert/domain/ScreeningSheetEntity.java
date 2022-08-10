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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ScreeningSheet")
@Table(name = "SCREENINGSHEET")
public class ScreeningSheetEntity implements Serializable {
    private static final long serialVersionUID = -9115350427459379777L;

    @Id
    @TableGenerator(name = "SEQ_SCREENINGSHEET", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_SCREENINGSHEET", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_SCREENINGSHEET")
    @Column(name = "SCREENINGSHEET_ID")
    private Long id;

    @Column(name = "DATA")
    private byte[] data;

    @OneToMany(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "SCREENINGSHEET_ID", referencedColumnName = "SCREENINGSHEET_ID", nullable = false)
    @OrderColumn(name = "POSITION")
    private List<ScreeningSheetParameterEntity> parameters;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "SELEKTIONSVEKTOR_ID")
    private SelektionsVektorEntity selektionsVektor;
}
