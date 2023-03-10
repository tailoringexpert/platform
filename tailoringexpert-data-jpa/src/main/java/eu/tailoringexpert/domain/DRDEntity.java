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
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.io.Serializable;

import static jakarta.persistence.GenerationType.TABLE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "DRD")
@Table(name = "DRD")
public class DRDEntity implements Serializable {
    private static final long serialVersionUID = 8011082379683283709L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_DRD", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_DRD", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_DRD")
    @Column(name = "DRD_ID")
    private Long id;

    /**
     * Number of the DRD.
     */
    @Column(name = "NUMBER")
    private String number;

    /**
     * Title of the DRD.
     */
    @Column(name = "TITLE")
    private String title;

    /**
     * Definition when to deliver the document.
     */
    @Column(name = "DELIVERYDATE")
    private String deliveryDate;

    /**
     * What to do.
     */
    @Column(name = "ACTION")
    private String action;
}
