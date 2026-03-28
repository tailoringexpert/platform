/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

import static jakarta.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "TailoringRequirementChangeEntity")
@Table(name = "TAILORINGREQUIREMENTCHANGE")
public class TailoringRequirementChangeEntity {

    @Id
    @TableGenerator(name = "SEQ_TAILORINGREQUIREMENTCHANGE", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_TAILORINGREQUIREMENTCHANGE", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_TAILORINGREQUIREMENTCHANGE")
    @Column(name = "REQUIREMENTCHANGE_ID")
    private Long id;

    @Column(name = "REQUIREMENT_ID", nullable = false)
    private Long requirementId;

    @Column(name = "CHANGETYPE")
    String changeType;

    @Column(name = "OLD")
    String old;

    @Column(name = "NEW")
    String changed;

    @Column(name = "USER_ID")
    String user;

    @Column(name = "MODIFICATIONTIMESTAMP", nullable = false)
    private ZonedDateTime modificationTimestamp;


}
