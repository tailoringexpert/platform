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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

import static java.util.Objects.nonNull;


@Value
@EqualsAndHashCode(callSuper = false)
@Relation(itemRelation = "signature", collectionRelation = "signatures")
public class DocumentSignatureResource extends RepresentationModel<DocumentSignatureResource> {

    /**
     * Name of the faculty.
     */
    private String faculty;

    /**
     * Name of the signee.
     */
    private String signee;

    /**
     * State of signature.
     */
    private DocumentSignatureState state;

    /**
     * State if signature shall be used/applicable.
     */
    private Boolean applicable;

    @Builder
    public DocumentSignatureResource(String faculty, String signee, DocumentSignatureState state, Boolean applicable, List<Link> links) {
        super();
        this.faculty = faculty;
        this.signee = signee;
        this.state = state;
        this.applicable = applicable;
        if (nonNull(links)) {
            add(links);
        }

    }
}
