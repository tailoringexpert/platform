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
package eu.tailoringexpert;

import eu.tailoringexpert.domain.DatenTyp;
import eu.tailoringexpert.domain.Parameter;
import lombok.Getter;
import lombok.extern.java.Log;

@Getter
@Log
public class ParameterDataProvider {

    private int lebensdauer = 3;
    private int designauslegung = 1;
    private int erfolg = 1;


    public static Parameter getEinsatzOrt() {
        return Parameter.builder()
            .kategorie("EINSATZORT")
            .name("LEO")
            .datenTyp(DatenTyp.MATRIX)
            .wert(new double[][]{{0.5, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0.7, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0.8, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0.6, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0.65, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0.8, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0.7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0.9}})
            .reihenfolge(1)
            .build();
    }

    public static Parameter getEinsatzZweck() {
        return Parameter.builder()
            .kategorie("EINSATZZWECK")
            .name("Erdbeobachtungssatellit")
            .datenTyp(DatenTyp.MATRIX)
            .wert(new double[][]{{0.8, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0.8, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0.9, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0.95}})
            .reihenfolge(2)
            .build();
    }

    public static Parameter getAnwendungscharacter() {
        return Parameter.builder()
            .kategorie("ANWENDUNGSCHARACTER")
            .name("wissenschaftlich")
            .datenTyp(DatenTyp.MATRIX)
            .wert(new double[][]{{0.5, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0.4, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0.6, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0.4, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0.5, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0.8, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0.7, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 1, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0.7}}).reihenfolge(3)
            .build();
    }

    public static Parameter getProduktTyp() {
        return Parameter.builder()
            .kategorie("PRODUKTTYP")
            .name("SAT")
            .datenTyp(DatenTyp.MATRIX)
            .wert(new double[][]{{10}, {10}, {10}, {10}, {10}, {10}, {10}, {10}, {10}, {10}})
            .reihenfolge(4)
            .build();
    }

    public static Parameter getLebensdauer() {
        return Parameter.builder()
            .kategorie("LEBENSDAUER")
            .name("15 Jahre < t")
            .datenTyp(DatenTyp.SKALAR)
            .wert(3)
            .reihenfolge(5)
            .build();
    }

    public static Parameter getProgammatischeBewertung() {
        return Parameter.builder()
            .kategorie("PROGRAMMATISCHE_BEWERTUNG")
            .name("erforderlich")
            .datenTyp(DatenTyp.SKALAR)
            .wert(1)
            .reihenfolge(6)
            .build();
    }

    public static Parameter getKosten() {
        return Parameter.builder()
            .kategorie("KOSTENORIENTIERUNG")
            .name("150 <= k")
            .datenTyp(DatenTyp.SKALAR)
            .wert(5)
            .reihenfolge(7)
            .build();
    }
}
