/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2025 Michael Bädorf and others
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

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

import static jakarta.persistence.GenerationType.TABLE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "ApplicableDocument")
@Table(name = "APPLICABLEDOCUMENT")
public class ApplicableDocumentEntity implements Serializable {
    private static final long serialVersionUID = 2277522795120520358L;

    /**
     * Technical ID.
     */
    @Id
    @TableGenerator(name = "SEQ_DOCUMENT", table = "SEQUENCE", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "SEQ_DOCUMENT", initialValue = 1)
    @GeneratedValue(strategy = TABLE, generator = "SEQ_DOCUMENT")
    @Column(name = "APPLICABLEDOCUMENT_ID")
    private Long id;

    /**
     * Number of the drd.
     */
    @Column(name = "NUMBER")
    private String number;

    /**
     * Title of the document.
     */
    @Column(name = "TITLE")
    private String title;

    /**
     * Issue of the document.
     */
    @Column(name = "ISSUE")
    private String issue;

    /**
     * Revision of the document
     */
    @Column(name="REVISION")
    private String revision;

    /**
     * Description of the document
     */
    @Column(name = "DESCRIPTION")
    private String description;
}




