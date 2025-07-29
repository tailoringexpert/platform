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
import lombok.Singular;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "BaseRequirement")
@Table(name = "BASEREQUIREMENT")
public class BaseRequirementEntity implements Serializable {
    private static final long serialVersionUID = -1863038272996727592L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_BASEREQUIREMENT", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_BASEREQUIREMENT", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_BASEREQUIREMENT")
    @Column(name = "REQUIREMENT_ID")
    private Long id;

    /**
     * Text of the requirement.
     */
    @Column(name = "TEXT")
    private String text;

    /**
     * Position (in chpater) of requirement.
     */
    @Column(name = "POSITION")
    private String position;

    /**
     * Requirement origin.
     */
    @Embedded
    @AssociationOverride(name = "logo",
        joinColumns = @JoinColumn(name = "REFERENCELOGO_ID"))
    private ReferenceEntity reference;

    /**
     * Identifiers this requirement shall be selected automatically.
     */
    @OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)
    @JoinColumn(name = "REQUIREMENT_ID", referencedColumnName = "REQUIREMENT_ID", nullable = false)
    private Set<IdentifierEntity> identifiers;

    /**
     * Phases the requirement belongs to.
     */
    @Singular(value = "phase", ignoreNullCollections = true)
    @ElementCollection
    @CollectionTable(
        name = "BASEREQUIREMENT_PHASE",
        joinColumns = @JoinColumn(name = "REQUIREMENT_ID")
    )
    @Column(name = "PHASE")
    @Enumerated(STRING)
    private List<Phase> phases;

    /**
     * List of applicable documents of the requirement.
     */
    @OneToMany(cascade = {DETACH, MERGE, PERSIST, REFRESH}, orphanRemoval = false, fetch = LAZY)
    @JoinTable(
        name = "BASEREQUIREMENT_APPLICABLEDOC",
        joinColumns = {@JoinColumn(name = "REQUIREMENT_ID", referencedColumnName = "REQUIREMENT_ID")},
        inverseJoinColumns = {@JoinColumn(name = "DOCUMENT_ID", referencedColumnName = "APPLICABLEDOCUMENT_ID")}
    )
    private List<ApplicableDocumentEntity> applicableDocuments;

    /**
     * List of drd requirement shall be part of.
     */
    @OneToMany(cascade = {DETACH, MERGE, PERSIST, REFRESH}, orphanRemoval = false, fetch = LAZY)
    @JoinTable(
        name = "BASEREQUIREMENT_DRD",
        joinColumns = {@JoinColumn(name = "REQUIREMENT_ID", referencedColumnName = "REQUIREMENT_ID")},
        inverseJoinColumns = {@JoinColumn(name = "DRD_ID", referencedColumnName = "DRD_ID")}
    )
    private Set<DRDEntity> drds;

    /**
     * Complete number containing full qualified chapter and postion.
     */
    @Column(name = "NUMBER")
    private String number;

}


