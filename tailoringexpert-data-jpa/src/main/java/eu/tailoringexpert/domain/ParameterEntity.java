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
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Parameter")
@Table(name = "PARAMETER")
public class ParameterEntity implements Serializable {
    private static final long serialVersionUID = -4411021507367220328L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_PARAMETER", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_PARAMETER", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_PARAMETER")
    @Column(name = "PARAMETER_ID")
    private Long id;

    /**
     * Category/group parameter belongs to.
     */
    @Column(name = "CATEGORY")
    private String category;

    /**
     * Label to show for parameter.
     */
    @Column(name = "LABEL")
    private String label;

    /**
     * (Technical)Name of the parameter.
     */
    @Column(name = "NAME")
    private String name;

    /**
     * Type of the value.
     */
    @Column(name = "PARAMETERTYPE")
    @Enumerated(STRING)
    private DatenType parameterType;

    /**
     * Value of the parameter.
     */
    @Column(name = "PARAMETERVALUE")
    private String value;

    /**
     * Ordering position.
     */
    @Column(name = "POSITION")
    private int position;
}
