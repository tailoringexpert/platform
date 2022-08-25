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
package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.domain.Parameter;

import java.util.Collection;

/**
 * Service for handling (peristent) data used by @see {@link ScreeningSheetService}.
 *
 * @author Michael Bädorf
 */
public interface ScreeningSheetServiceRepository {

    /**
     * Retrieve objects identified by provided names.
     *
     * @param names Names of parameter to get objects of
     * @return Colletion of parameter objects
     */
    Collection<Parameter> getParameter(Collection<String> names);
}
