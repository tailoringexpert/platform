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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Project implements Serializable {
    private static final long serialVersionUID = 3370202290719541977L;

    private String identifier;
    private ScreeningSheet screeningSheet;
    private String catalogVersion;
    private ZonedDateTime creationTimestamp;

    @Singular(value = "tailoring", ignoreNullCollections = true)
    private Collection<Tailoring> tailorings = new ArrayList<>();

    public void setScreeningSheet(ScreeningSheet screeningSheet) {
        this.screeningSheet = screeningSheet;
        this.identifier = this.screeningSheet.getProject();
    }
}
