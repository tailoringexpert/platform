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

import java.util.Collection;
import java.util.List;

import static java.util.Objects.nonNull;

@Value
@Getter
@EqualsAndHashCode(callSuper = false)
public class TailoringResource extends RepresentationModel<TailoringResource> {

    String name;
    Collection<Phase> phasen;
    TailoringStatus status;
    KatalogResource katalog;

    @Builder
    public TailoringResource(String name, Collection<Phase> phasen, TailoringStatus status, KatalogResource katalog, List<Link> links) {
        super();
        this.name = name;
        this.phasen = phasen;
        this.status = status;
        this.katalog = katalog;
        if (nonNull(links)) {
            add(links);
        }
    }
}
