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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.util.List;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.TABLE;

@Data
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "KatalogKapitel")
@Table(name = "BASECATALOGCHAPTER")
public class KatalogKapitelEntity implements Serializable {
    private static final long serialVersionUID = 8843674935264907440L;

    @Id
    @TableGenerator(name = "SEQ_BASECATALOGCHAPTER", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_BASECATALOGCHAPTER", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_BASECATALOGCHAPTER")
    @Column(name = "CHAPTER_ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "POSITION")
    private int position;

    @Column(name = "NUMBER")
    private String nummer;

    @OneToMany(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "PARENTCHAPTER_ID", referencedColumnName = "CHAPTER_ID")
    @OrderColumn(name = "CHAPTER_ORDER")
    private List<KatalogKapitelEntity> kapitel;

    @OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)
    @JoinColumn(name = "CHAPTER_ID", referencedColumnName = "CHAPTER_ID", nullable = false)
    @OrderColumn(name = "REQUIREMENT_ORDER")
    private List<KatalogAnforderungEntity> anforderungen;

}
