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

import static java.util.Objects.nonNull;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = false)
@Relation(itemRelation = "matrix", collectionRelation = "matrices")
public class MatrixFileResource extends RepresentationModel<MatrixFileResource> {
    private static final long serialVersionUID = 953716401L;

    /**
     * Description of intended mission type.
     */
    private String description;

    /**
     * Description of intended mission type.
     */
    private String name;

    /**
     * Catalogue version used for creating matrix file.
     */
    private String catalogueVersion;

    /**
     * Creation timestamp of matrix.
     */
    private String creationTimestamp;

    /**
     * Hash of matrix document.
     */
    private String hash;

    @Builder
    public MatrixFileResource(String description, String name, String catalogueVersion, String creationTimestamp,
            String hash,
            List<Link> links) {
        super();
        this.description = description;
        this.name = name;
        this.catalogueVersion = catalogueVersion;
        this.hash = hash;
        this.creationTimestamp = creationTimestamp;
        if (nonNull(links)) {
            add(links);
        }
    }

}
