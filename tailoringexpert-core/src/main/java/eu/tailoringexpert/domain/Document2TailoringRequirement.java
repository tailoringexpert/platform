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

import java.util.function.Function;

import static java.util.Objects.nonNull;

public class Document2TailoringRequirement implements Function<Document, TailoringRequirement> {
    @Override
    public TailoringRequirement apply(Document document) {
        return TailoringRequirement.builder()
            .position(document.getNumber())
            .text(document.getTitle() +
                (nonNull(document.getIssue()) ? document.getIssue() : null) +
                (nonNull(document.getRevision()) ? document.getRevision() : null)
            )
            .build();
    }
}
