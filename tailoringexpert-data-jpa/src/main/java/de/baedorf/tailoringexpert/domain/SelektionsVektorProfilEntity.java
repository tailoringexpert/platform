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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.util.Map;

import static javax.persistence.GenerationType.TABLE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "SelektionsVektorProfil")
@Table(name = "SELEKTIONSVEKTORPROFIL")
public class SelektionsVektorProfilEntity implements Serializable {
    private static final long serialVersionUID = -5062851233486910911L;

    @Id
    @TableGenerator(name = "SEQ_SELEKTIONSVEKTORPROFIL", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_SELEKTIONSVEKTORPROFIL", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_SELEKTIONSVEKTORPROFIL")
    @Column(name = "SELEKTIONSVEKTORPROFIL_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @ElementCollection
    @MapKeyColumn(name = "TYP")
    @Column(name = "LEVEL")
    @CollectionTable(name = "SELEKTIONSVEKTORPROFILPARAMETER", joinColumns = @JoinColumn(name = "SELEKTIONSVEKTORPROFIL_ID"))
    private Map<String, Integer> levels;

}

