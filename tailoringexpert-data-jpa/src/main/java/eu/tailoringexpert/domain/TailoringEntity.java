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

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Tailoring")
@Table(name = "TAILORING")
public class TailoringEntity implements Serializable {
    private static final long serialVersionUID = -2503249103618921192L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_TAILORING", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_TAILORING", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_TAILORING")
    @Column(name = "TAILORING_ID")
    private Long id;

    /**
     * Identifier used as doc id.
     */
    @Column(name = "IDENTIFER")
    private String identifier;

    /**
     * Name of the tailoring.
     */
    @Column(name = "NAME")
    private String name;

    /**
     * Applied selectionvector.
     */
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "SELECTIONVECTOR_ID")
    private SelectionVectorEntity selectionVector;

    /**
     * Input screeningsheet.
     */
    @OneToOne(cascade = ALL)
    @JoinColumn(name = "SCREENINGSHEET_ID")
    private ScreeningSheetEntity screeningSheet;

    /**
     * Phases of tailoring.
     */
    @Singular("phase")
    @ElementCollection
    @CollectionTable(
        name = "TAILORING_PHASE",
        joinColumns = @JoinColumn(name = "TAILORING_ID")
    )
    @Enumerated(STRING)
    @Column(name = "PHASE")
    @OrderColumn(name = "PHASE_ORDER")
    private List<Phase> phases;

    /**
     * Complete catalog of requirements with applicated state.
     */
    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "TAILORINGCATALOG_ID")
    private TailoringCatalogEntity catalog;

    /**
     * State of tailoring.
     */
    @Enumerated(STRING)
    @Column(name = "STATE")
    private TailoringState state;

    /**
     * Signatures to be used for document generation.
     */
    @ElementCollection
    @CollectionTable(
        name = "DOCUMENTSIGNATURE",
        joinColumns = @JoinColumn(name = "TAILORING_ID")
    )
    private Collection<DocumentSignatureEntity> signatures;

    /**
     * Notes of tailoring.
     */
    @OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)
    @JoinColumn(name = "TAILORING_ID", referencedColumnName = "TAILORING_ID", nullable = false)
    private Collection<NoteEntity> notes;

    /**
     * Creation timestamp of tailoring.
     */
    @Column(name = "CREATIONTIMESTAMP")
    private ZonedDateTime creationTimestamp;
}
