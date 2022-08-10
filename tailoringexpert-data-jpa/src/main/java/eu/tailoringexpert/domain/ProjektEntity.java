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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Projekt")
@Table(name = "PROJECT")
public class ProjektEntity implements Serializable {
    private static final long serialVersionUID = -7657514213994672871L;

    @Id
    @TableGenerator(name = "SEQ_PROJECT", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_PROJECT", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_PROJECT")
    @Column(name = "PROJECT_ID")
    private Long id;

    @Column(name = "IDENTIFIER")
    private String kuerzel;

    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "SCREENINGSHEET_ID")
    private ScreeningSheetEntity screeningSheet;

    @OneToMany(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "PROJECT_ID", referencedColumnName = "PROJECT_ID", nullable = false)
    @OrderColumn(name = "TAILORING_ORDER")
    private List<TailoringEntity> tailorings = new ArrayList<>();

    @Column(name = "CREATIONTIMESTAMP")
    private ZonedDateTime erstellungsZeitpunkt;

    public Optional<TailoringEntity> getTailoring(String name) {
        if (name == null) {
            return Optional.empty();
        }

        return getTailorings().stream()
            .filter(phase -> phase.getName().equals(name))
            .findFirst();
    }
}
