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

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.io.Serializable;

import static jakarta.persistence.GenerationType.TABLE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "ScreeningSheetParameter")
@Table(name = "SCREENINGSHEETPARAMETER")
public class ScreeningSheetParameterEntity implements Serializable {
    private static final long serialVersionUID = 626569218667788612L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_SCREENINGSHEETPARAMETER", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_SCREENINGSHEETPARAMETER", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_SCREENINGSHEETPARAMETER")
    @Column(name = "SCREENINGSHEETPARAMETER_ID")
    private Long id;

    /**
     * Category/group parameter belongs to.
     */
    @Column(name = "CATEGORY")
    private String category;

    /**
     * Value of the parameter.
     */
    @SuppressWarnings({"java:S1948"})
    @Column(name = "PARAMETERVALUE")
    @Convert(converter = ScreeningSheetParameterValueAttributeConverter.class)
    private Object value;

}
