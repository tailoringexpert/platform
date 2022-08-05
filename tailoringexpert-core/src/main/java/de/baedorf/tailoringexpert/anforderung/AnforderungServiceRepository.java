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

public interface AnforderungServiceRepository {

    /**
     * Ermittelt die durch Kapitel und Position definierte Anforderung.
     *
     * @param projekt   Projekt, zum dem die Anforderung gehört
     * @param tailoring Tailoring des Projekts
     * @param kapitel   Kapitel, in der sich die Anforderung befindet
     * @param position  Postion der Anforderung im Kapitel
     * @return Die ermittelte Anforderung
     */
    Optional<TailoringAnforderung> getAnforderung(String projekt, String tailoring, String kapitel, String position);

    /**
     * Ermittelt die durch das Kapitel definierte Anforderungsgruppe.
     *
     * @param projekt   Projekt, zum dem die Anforderung gehört
     * @param tailoring Tailoring des Projekts
     * @param kapitel   Kapitel der zu ermittelnden Anforderungsgruppe
     * @return Das ermittelte Kapitel
     */
    Optional<Kapitel<TailoringAnforderung>> getKapitel(String projekt, String tailoring, String kapitel);

    /**
     * Aktualisiert die übergebene Anforderung.
     *
     * @param projekt     Projekt der Anforderung
     * @param tailoring   Projektphase
     * @param kapitel     Kapitel, zu der die Anforderung gehört
     * @param anforderung Die zu aktualisierende Anforderung
     * @return Die aktualisierte Anforderung
     */
    Optional<TailoringAnforderung> updateAnforderung(String projekt, String tailoring, String kapitel, TailoringAnforderung anforderung);

    /**
     * Aktualisierung der ausgewaehlt Informationen aller direkten und nachfolgenden Anforderungen der Gruppe.
     *
     * @param projekt   Projekt, zum dem die Anforderung gehört
     * @param tailoring Tailoring des Projekts
     * @param kapitel   Kapitel, deren Anforderungen aktualisiert werden sollen
     * @return Das aktualisierte Kapitel
     */
    Optional<Kapitel<TailoringAnforderung>> updateAusgewaehlt(String projekt, String tailoring, Kapitel<TailoringAnforderung> kapitel);

    /**
     * Aktualisierert das übergebene Kapitel.
     *
     * @param projekt   Projekt, zum dem die Anforderung gehört
     * @param tailoring Tailoring des Projekts
     * @param kapitel   Gruppe, deren Anforderungen aktualisiert werden sollen
     * @return Das aktualisierte Kapitel
     */
    Optional<Kapitel<TailoringAnforderung>> updateKapitel(String projekt, String tailoring, Kapitel<TailoringAnforderung> kapitel);
}
