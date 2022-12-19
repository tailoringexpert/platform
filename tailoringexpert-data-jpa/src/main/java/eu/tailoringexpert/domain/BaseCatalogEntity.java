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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.time.ZonedDateTime;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.TABLE;


/**
 * Mongo Repräsentation eines Artikels.
 *
 * @author Michael Bädorf
 */
@Data
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Catalog")
@Table(name = "BASECATALOG")
public class BaseCatalogEntity implements Serializable {
    private static final long serialVersionUID = -6960103411041929370L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_BASECATALOG", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_BASECATALOG", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_BASECATALOG")
    @Column(name = "CATALOG_ID")
    private Long id;

    /**
     * Version of the catalog.
     */
    @Column(name = "VERSION")
    private String version;

    /**
     * Date catalog validity starts.
     */
    @Column(name = "VALIDFROM")
    private ZonedDateTime validFrom;

    /**
     * Date catalog validity ends.
     */
    @Column(name = "VALIDUNTIL")
    private ZonedDateTime validUntil;

    /**
     * Table of contents of catalog.
     */
    @OneToOne(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "CHAPTER_ID")
    private BaseCatalogChapterEntity toc;

}


