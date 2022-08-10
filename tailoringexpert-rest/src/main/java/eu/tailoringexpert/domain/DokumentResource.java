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

import java.util.List;

import static java.util.Objects.nonNull;


@Value
@EqualsAndHashCode(callSuper = false)
public class DokumentResource extends RepresentationModel<DokumentResource> {

    private String name;
    private String typ;
    private String hash;

    @Builder
    public DokumentResource(String name, String typ, String hash, List<Link> links) {
        super();
        this.name = name;
        this.typ = typ;
        this.hash = hash;
        if (nonNull(links)) {
            add(links);
        }

    }
}
