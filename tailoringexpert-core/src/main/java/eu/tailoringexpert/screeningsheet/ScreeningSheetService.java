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

public interface ScreeningSheetService {

    /**
     * Berechnung des Selektionsvektors anhand des übergebenen Screeningsheets.
     *
     * @param rawData (PDF) Rohdaten des Screeningsheets
     * @return Der berechnete Selektionsvektor
     */
    SelectionVector calculateSelectionVector(byte[] rawData);

    /**
     * Erzeugt ein ScreeningSheet Objekt inklusive berechnetem Selektionsvektor
     *
     * @param rawData (PDF) Rohdaten des Screeningsheets
     * @return Screeningsheet mit berechnetem Selektionsvektor
     */
    ScreeningSheet createScreeningSheet(byte[] rawData);
}
