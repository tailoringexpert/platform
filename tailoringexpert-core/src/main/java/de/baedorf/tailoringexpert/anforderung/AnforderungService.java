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
package de.baedorf.tailoringexpert.anforderung;

import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;

import java.util.Optional;

public interface AnforderungService {

    /**
     * Ändert den ausgewählt Status einer Anforderung.
     *
     * @param projekt     Projekt, zum dem die Anforderung gehört
     * @param tailoring   Tailoring des Projekts
     * @param kapitel     Kapitel, aus dem die Anforderung stammt
     * @param position    Position der Anforderung im Kapitel
     * @param ausgewaehlt Der neue ausgwählt Status der Anforderung
     * @return Die geänderte Anforderung
     */
    Optional<TailoringAnforderung> handleAusgewaehlt(String projekt, String tailoring, String kapitel, String position, Boolean ausgewaehlt);

    /**
     * Ändert den ausgewählt Status einer Anforderungen im Kapitel sowie dessen Unterkapiteln.
     *
     * @param projekt     Projekt, zum dem die Anforderung gehört
     * @param tailoring   Tailoring des Projekts
     * @param kapitel     Kapitel, der zu ändernden Auswahl aller  Anforderungen und Unteranforderungen
     * @param ausgewaehlt Der neue ausgwählt Status der Anforderung
     * @return Kapitel der geänderten Anforderungen
     */
    Optional<Kapitel<TailoringAnforderung>> handleAusgewaehlt(String projekt, String tailoring, String kapitel, Boolean ausgewaehlt);

    /**
     * Ändert den Text  einer Anforderung.
     *
     * @param projekt   Projekt, zum dem die Anforderung gehört
     * @param tailoring Tailoring des Projekts
     * @param kapitel   Kapitel, aus dem die Anforderung stammt
     * @param position  Position der Anforderung im Kapitel
     * @param text      Der neue Text der Anforderung
     * @return Die geänderte Anforderung
     */
    Optional<TailoringAnforderung> handleText(String projekt, String tailoring, String kapitel, String position, String text);

    /**
     * Erstellt eine neue Projekt-Anforderung im angegeben Projekt an der Position NACH der überegebenen Position
     *
     * @param projekt   Projekt, zum dem die Anforderung hinzugefügt
     * @param tailoring Tailoring des Projekts
     * @param kapitel   Kapitel, in dem die Anforderung hinzugefügt werden soll
     * @param position  Position, NACH der die neue Anforderung im erstellt werden soll
     * @param text      Der Text der neuen Anforderung
     * @return Die neu erstelte Anforderung
     */
    Optional<TailoringAnforderung> createAnforderung(String projekt, String tailoring, String kapitel, String position, String text);
}
