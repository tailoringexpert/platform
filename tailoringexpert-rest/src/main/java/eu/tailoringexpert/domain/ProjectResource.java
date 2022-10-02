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
import lombok.Getter;
import lombok.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.nonNull;

@Value
@Getter
@EqualsAndHashCode(callSuper = false)
@Relation(itemRelation = "project", collectionRelation = "projects")
public class ProjectResource extends RepresentationModel<ProjectResource> {

    String name;
    String projectManager;
    Collection<TailoringResource> tailorings;
    String creationTimestamp;
    String catalogVersion;

    @Builder
    public ProjectResource(String name, String projectManager, String creationTimestamp, Collection<TailoringResource> tailorings, String catalogVersion, List<Link> links) {
        super();
        this.name = name;
        this.projectManager = projectManager;
        this.creationTimestamp = creationTimestamp;
        this.tailorings = tailorings;
        this.catalogVersion = catalogVersion;
        if (nonNull(links)) {
            add(links);
        }
    }
}
