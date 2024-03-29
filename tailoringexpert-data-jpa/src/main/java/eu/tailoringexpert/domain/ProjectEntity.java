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

import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.EnumType.STRING;
import static java.util.Objects.isNull;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Project")
@Table(name = "PROJECT")
public class ProjectEntity implements Serializable {
    private static final long serialVersionUID = -7657514213994672871L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_PROJECT", table = "SEQUENCE", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_PROJECT", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_PROJECT")
    @Column(name = "PROJECT_ID")
    private Long id;

    /**
     * Unique identifier of project.
     */
    @Column(name = "IDENTIFIER")
    private String identifier;

    /**
     * Screeningsheet used to create project.
     */
    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "SCREENINGSHEET_ID")
    private ScreeningSheetEntity screeningSheet;

    /**
     * Tailorings of the project.
     */
    @OneToMany(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "PROJECT_ID", referencedColumnName = "PROJECT_ID", nullable = false)
    @OrderColumn(name = "TAILORING_ORDER")
    private List<TailoringEntity> tailorings = new ArrayList<>();

    /**
     * Creation timestamp of project.
     */
    @Column(name = "CREATIONTIMESTAMP")
    private ZonedDateTime creationTimestamp;

    /**
     * State of tailoring.
     */
    @Enumerated(STRING)
    @Column(name = "STATE")
    private ProjectState state;

    /**
     * Find a tailoring by given name.
     *
     * @param name Name of the tailoring to get
     * @return requested tailoring if exsists, otherwise empty
     */
    public Optional<TailoringEntity> getTailoring(String name) {
        if (isNull(name)) {
            return Optional.empty();
        }

        return getTailorings().stream().filter(phase -> phase.getName().equals(name)).findFirst();
    }
}
