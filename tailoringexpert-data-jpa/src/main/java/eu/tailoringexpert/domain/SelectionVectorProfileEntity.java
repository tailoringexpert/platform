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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.io.Serializable;
import java.util.Map;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.GenerationType.TABLE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "SelectionVectorProfile")
@Table(name = "SELECTIONVECTORPROFILE")
public class SelectionVectorProfileEntity implements Serializable {
    private static final long serialVersionUID = -5062851233486910911L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_SELECTIONVECTORPROFILE", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_SELECTIONVECTORPROFILE", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_SELECTIONVECTORPROFILE")
    @Column(name = "SELECTIONVECTORPROFILE_ID")
    private Long id;

    /**
     * (Display) Name of the profile.
     */
    @Column(name = "NAME")
    private String name;

    /**
     * Internal (business) key of profile
     */
    @Column(name = "INTERNALKEY")
    private String internalKey;
    /**
     * Mapping between types and levels.
     */
    @MapKeyColumn(name = "TYPE")
    @Column(name = "LEVEL")
    @CollectionTable(name = "SELECTIONVECTORPROFILEPARAMETER", joinColumns = @JoinColumn(name = "SELECTIONVECTORPROFILE_ID"))
    @ElementCollection(fetch = EAGER)
    private Map<String, Integer> levels;
}

