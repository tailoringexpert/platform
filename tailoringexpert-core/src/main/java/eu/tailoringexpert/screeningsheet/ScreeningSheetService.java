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

import eu.tailoringexpert.domain.ScreeningSheet;
import eu.tailoringexpert.domain.SelectionVector;

/**
 * Service for handling screeningsheets and their data.
 *
 * @author Michael Bädorf
 */
public interface ScreeningSheetService {

    /**
     * Calculate selectionvector based on screeningsheet data.
     *
     * @param rawData (PDF) of screeningsheet
     * @return calculated seelctionvector
     */
    SelectionVector calculateSelectionVector(byte[] rawData);

    /**
     * Creates a screensheet data object with a calculated selectiovector.
     *
     * @param rawData (PDF) of screeningsheet
     * @return Screeningsheet with calculated selectionvector
     */
    ScreeningSheet createScreeningSheet(byte[] rawData);
}
