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

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.TABLE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "TailoringRequirement")
@Table(name = "TAILORINGREQUIREMENT")
public class TailoringRequirementEntity implements Serializable {
    private static final long serialVersionUID = 586167029258031537L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_TAILORINGREQUIREMENT", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_TAILORINGREQUIREMENT", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_TAILORINGREQUIREMENT")
    @Column(name = "REQUIREMENT_ID")
    private Long id;

    /**
     * Text of the requirement.
     */
    @Column(name = "TEXT")
    private String text;

    /**
     * Requirement origin.
     */
    @Embedded
    @AssociationOverride(name = "logo",
        joinColumns = @JoinColumn(name = "REFERENCELOGO_ID"))
    private ReferenceEntity reference;

    /**
     * Position (in chpater) of requirement.
     */
    @Column(name = "POSITION")
    private String position;

    /**
     * List of DRDs requirement shall be part of.
     */
    @OneToMany(fetch = LAZY)
    @JoinTable(
        name = "TAILORINGREQUIREMENT_DRD",
        joinColumns = {@JoinColumn(name = "REQUIREMENT_ID", referencedColumnName = "REQUIREMENT_ID")},
        inverseJoinColumns = {@JoinColumn(name = "DRD_ID", referencedColumnName = "DRD_ID")}
    )
    private Set<DRDEntity> drds;

    /**
     * State if requirement is selected.
     */
    @Column(name = "SELECTED")
    private Boolean selected;

    /**
     * Time when selection state was changed.
     */
    @Column(name = "SELECTIONCHANGED")
    private ZonedDateTime selectionChanged;

    /**
     * Time when requirement text was changed.
     */
    @Column(name = "TEXTCHANGED")
    private ZonedDateTime textChanged;
}


