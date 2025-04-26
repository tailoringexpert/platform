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

import lombok.NonNull;

import java.util.Comparator;

public class DocumentNumberComparator implements Comparator<Document> {
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(@NonNull Document reference, @NonNull Document compare) {
        int referenceLength = reference.getNumber().length();
        int compareLength = compare.getNumber().length();

        if (referenceLength == compareLength) {
            return reference.getNumber().compareToIgnoreCase(compare.getNumber());
        }

        return referenceLength < compareLength ? -1 : 1;
    }
}
