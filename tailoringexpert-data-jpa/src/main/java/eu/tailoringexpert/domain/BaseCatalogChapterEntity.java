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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import java.io.Serializable;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.TABLE;

@Data
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "BaseCatalogChapter")
@Table(name = "BASECATALOGCHAPTER")
public class BaseCatalogChapterEntity implements Serializable {
    private static final long serialVersionUID = 8843674935264907440L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_BASECATALOGCHAPTER", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_BASECATALOGCHAPTER", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_BASECATALOGCHAPTER")
    @Column(name = "CHAPTER_ID")
    private Long id;

    /**
     * Name of chapter.
     */
    @Column(name = "NAME")
    private String name;

    /**
     * Position in chapter list.
     */
    @Column(name = "POSITION")
    private int position;

    /**
     * (Full) Number of chapter.
     */
    @Column(name = "NUMBER")
    private String number;

    /**
     * List of subchapters.
     */
    @OneToMany(cascade = ALL, fetch = LAZY)
    @JoinColumn(name = "PARENTCHAPTER_ID", referencedColumnName = "CHAPTER_ID")
    @OrderColumn(name = "CHAPTER_ORDER")
    private List<BaseCatalogChapterEntity> chapters;

    /**
     * Requirements defined in chapter.
     */
    @OneToMany(cascade = ALL, orphanRemoval = true, fetch = LAZY)
    @JoinColumn(name = "CHAPTER_ID", referencedColumnName = "CHAPTER_ID", nullable = false)
    @OrderColumn(name = "REQUIREMENT_ORDER")
    private List<BaseRequirementEntity> requirements;

}
