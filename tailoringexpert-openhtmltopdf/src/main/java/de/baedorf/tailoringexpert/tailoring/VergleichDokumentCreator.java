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
package de.baedorf.tailoringexpert.tailoring;

import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Tailoring;
import de.baedorf.tailoringexpert.domain.TailoringAnforderung;
import de.baedorf.tailoringexpert.renderer.HTMLTemplateEngine;
import de.baedorf.tailoringexpert.renderer.PDFEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Log4j2
public class VergleichDokumentCreator implements DokumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    /**
     * {@inheritDoc}
     */
    @Override
    public Datei createDokument(@NonNull String docId,
                                @NonNull Tailoring tailoring,
                                @NonNull Map<String, String> platzhalter) {
        try {
            Map<String, Object> parameter = new HashMap<>(platzhalter);

            Collection<VergleichElement> anforderungen = new LinkedList<>();
            parameter.put("screeningsheet", tailoring.getScreeningSheet().getParameters());

            parameter.put("anforderungen", anforderungen);
            if (nonNull(tailoring.getKatalog().getToc())) {
                tailoring.getKatalog().getToc().getKapitel()
                    .forEach(gruppe -> addGruppe(gruppe, anforderungen));
            }

            String html = templateEngine.process(tailoring.getKatalog().getVersion() + "/vergleich", parameter);

            return pdfEngine.process(docId, html, tailoring.getKatalog().getVersion());
        } catch (Exception e) {
            log.catching(e);
        }
        return null;
    }

    void addGruppe(Kapitel<TailoringAnforderung> gruppe, Collection<VergleichElement> zeilen) {
        zeilen.add(VergleichElement.builder()
            .absatz(templateEngine.toXHTML(gruppe.getNummer(), emptyMap()))
            .titel(templateEngine.toXHTML(gruppe.getName(), emptyMap()))
            .build());
        gruppe.getAnforderungen()
            .forEach(anforderung -> addAnforderung(anforderung, zeilen));
        if (nonNull(gruppe.getKapitel())) {
            gruppe.getKapitel()
                .forEach(subgroup -> addGruppe(subgroup, zeilen));
        }
    }

    void addAnforderung(TailoringAnforderung anforderung, Collection<VergleichElement> zeilen) {
        zeilen.add(VergleichElement.builder()
            .absatz(templateEngine.toXHTML(anforderung.getPosition(), emptyMap()))
            .ausgewaehlt(anforderung.getAusgewaehlt())
            .geaendert(nonNull(anforderung.getAusgewaehltGeaendert()))
            .datum(anforderung.getAusgewaehltGeaendert())
            .build());
    }
}
