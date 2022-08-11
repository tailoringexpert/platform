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

import javax.persistence.AssociationOverride;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "KatalogAnforderung")
@Table(name = "BASECATALOGREQUIREMENT")
public class KatalogAnforderungEntity implements Serializable {
    private static final long serialVersionUID = -1863038272996727592L;

    @Id
    @TableGenerator(name = "SEQ_BASECATALOGREQUIREMENT", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_BASECATALOGREQUIREMENT", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_BASECATALOGREQUIREMENT")
    @Column(name = "REQUIREMENT_ID")
    private Long id;

    @Column(name = "TEXT")
    private String text;

    @Embedded
    @AssociationOverride(name = "logo",
        joinColumns = @JoinColumn(name = "REFERENCELOGO_ID"))
    private ReferenzEntity referenz;

    @Column(name = "POSITION")
    private String position;

    @Singular(value = "phase", ignoreNullCollections = true)
    @ElementCollection
    @CollectionTable(
        name = "BASECATALOGREQUIREMENT_PHASE",
        joinColumns = @JoinColumn(name = "REQUIREMENT_ID")
    )
    @Column(name = "PHASE")
    @Enumerated(STRING)
    private List<Phase> phasen;

    @OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)
    @JoinColumn(name = "REQUIREMENT_ID", referencedColumnName = "REQUIREMENT_ID", nullable = false)
    private Set<IdentifikatorEntity> identifikatoren;

    @OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)
    @JoinTable(
        name = "BASECATALOGREQUIREMENT_DRD",
        joinColumns = {@JoinColumn(name = "REQUIREMENT_ID", referencedColumnName = "REQUIREMENT_ID")},
        inverseJoinColumns = {@JoinColumn(name = "DRD_ID", referencedColumnName = "DRD_ID")}
    )
    private Set<DRDEntity> drds;


}


