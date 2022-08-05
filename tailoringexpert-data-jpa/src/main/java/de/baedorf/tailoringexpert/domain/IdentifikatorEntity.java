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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.util.Set;

import static javax.persistence.GenerationType.TABLE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Identifikator")
@Table(name = "IDENTIFIKATOR")
public class IdentifikatorEntity implements Serializable {
    private static final long serialVersionUID = 7005585376849837188L;

    @Id
    @TableGenerator(name = "SEQ_IDENTIFIKATOR", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_IDENTIFIKATOR", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_IDENTIFIKATOR")
    @Column(name = "IDENTIFIKATOR_ID")
    private Long id;

    @Column(name = "TYP")
    private String typ;

    @Column(name = "LEVEL")
    private int level;

    @ElementCollection
    @CollectionTable(
        name = "IDENTIFIKATOR_LIMITIERUNG",
        joinColumns = @JoinColumn(name = "IDENTIFIKATOR_ID")
    )
    @Column(name = "LIMITIERUNG")
    private Set<String> limitierungen;

}
