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

import de.baedorf.tailoringexpert.domain.DRD;
import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.Phase;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

/**
 * Erzeugt eine neue PDF Compliance Matrix.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
public class CMDokumentCreator implements DokumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    @NonNull
    private BiFunction<Kapitel<TailoringAnforderung>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public Datei createDokument(String docId,
                                Tailoring tailoring,
                                Map<String, String> platzhalter) {
        Map<String, Object> parameter = new HashMap<>(platzhalter);
        parameter.put("zeichnungen", tailoring.getZeichnungen());

        Collection<CMElement> kapitel = new LinkedList<>();
        parameter.put("kapitels", kapitel);

        Collection<DRDElement> drds = new LinkedList<>();
        parameter.put("drds", drds);

        Katalog<TailoringAnforderung> katalog = tailoring.getKatalog();
        katalog.getToc().getKapitel()
            .forEach(gruppe -> addKapitel(gruppe, 1, kapitel, platzhalter));
        addDRD(katalog.getToc(), drds, tailoring.getPhasen());

        String html = templateEngine.process(katalog.getVersion() + "/cm", parameter);

        return pdfEngine.process(docId, html, tailoring.getKatalog().getVersion());
    }

    void addKapitel(Kapitel<TailoringAnforderung> kapitel, int ebene, Collection<CMElement> zeilen, Map<String, String> platzhalter) {
        zeilen.add(CMElement.builder()
            .ebene(ebene)
            .nummer(templateEngine.toXHTML(kapitel.getNummer(), emptyMap()))
            .name(templateEngine.toXHTML(kapitel.getName(), platzhalter))
            .build());
        if (nonNull(kapitel.getKapitel())) {
            AtomicInteger naechsteEbene = new AtomicInteger(ebene + 1);
            kapitel.getKapitel()
                .forEach(subgroup -> addKapitel(subgroup, naechsteEbene.get(), zeilen, platzhalter));
        }
    }

    void addDRD(Kapitel<TailoringAnforderung> gruppe, Collection<DRDElement> zeilen, Collection<Phase> phasen) {
        drdProvider.apply(gruppe, phasen)
            .entrySet()
            .forEach(entry -> zeilen.add(DRDElement.builder()
                .titel(entry.getKey().getTitel())
                .datum(entry.getKey().getLieferzeitpunkt())
                .anforderung(entry.getValue())
                .nummer(entry.getKey().getNummer())
                .aktion(entry.getKey().getAktion())
                .build()));
    }

}
