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

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
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
@Entity(name = "Tailoring")
@Table(name = "TAILORING")
public class TailoringEntity implements Serializable {
    private static final long serialVersionUID = -2503249103618921192L;

    @Id
    @TableGenerator(name = "SEQ_TAILORING", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_TAILORING", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_TAILORING")
    @Column(name = "TAILORING_ID")
    private Long id;

    @Column(name = "KENNUNG")
    private String kennung;

    @Column(name = "NAME")
    private String name;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "SELEKTIONSVEKTOR_ID")
    private SelektionsVektorEntity selektionsVektor;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "KATALOGDEFINITION_ID")
    private KatalogEntity basisKatalog;

    @OneToOne(cascade = ALL)
    @JoinColumn(name = "SCREENINGSHEET_ID")
    private ScreeningSheetEntity screeningSheet;

    @Singular("phase")
    @ElementCollection
    @CollectionTable(
        name = "TAILORING_PHASE",
        joinColumns = @JoinColumn(name = "TAILORING_ID")
    )
    @Enumerated(STRING)
    @Column(name = "PHASE")
    @OrderColumn(name = "PHASE_ORDER")
    private List<Phase> phasen;

    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "KATALOG_ID")
    private TailoringKatalogEntity katalog;

    @Enumerated(STRING)
    @Column(name = "STATUS")
    private TailoringStatus status;

    @ElementCollection
    @CollectionTable(
        name = "DOKUMENTZEICHNUNG",
        joinColumns = @JoinColumn(name = "TAILORING_ID")
    )
    private Collection<DokumentZeichnungEntity> zeichnungen;

    @OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)
    @JoinColumn(name = "TAILORING_ID", referencedColumnName = "TAILORING_ID", nullable = false)
    private Set<DokumentEntity> dokumente;

    @Column(name = "ERSTELLUNGSZEITPUNKT")
    private ZonedDateTime erstellungsZeitpunkt;
}
