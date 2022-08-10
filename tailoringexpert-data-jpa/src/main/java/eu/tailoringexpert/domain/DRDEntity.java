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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;

import static javax.persistence.GenerationType.TABLE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "DRD")
@Table(name = "DRD")
public class DRDEntity implements Serializable {
    private static final long serialVersionUID = 8011082379683283709L;

    @Id
    @TableGenerator(name = "SEQ_DRD", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_DRD", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_DRD")
    @Column(name = "DRD_ID")
    private Long id;

    @Column(name = "NUMMER")
    private String nummer;

    @Column(name = "TITEL")
    private String titel;

    @Column(name = "LIEFERZEITPUNKT")
    private String lieferzeitpunkt;

    @Column(name = "AKTION")
    private String aktion;

}
