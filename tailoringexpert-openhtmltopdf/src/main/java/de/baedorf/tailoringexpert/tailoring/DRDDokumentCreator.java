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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor
@Log4j2
public class DRDDokumentCreator implements DokumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    @NonNull
    private BiFunction<Kapitel<TailoringAnforderung>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    @Override
    public Datei createDokument(String docId,
                                Tailoring tailoring,
                                Map<String, String> platzhalter) {
        log.traceEntry("Start creating DRD document {}", docId);

        Map<String, Object> parameter = new HashMap<>(platzhalter);
        parameter.put("katalogVersion", tailoring.getKatalog().getVersion());

        Collection<DRDFragment> drds = new LinkedList<>();
        parameter.put("drds", drds);

        Katalog<TailoringAnforderung> katalog = tailoring.getKatalog();
        if (nonNull(katalog.getToc())) {
            addDRD(katalog.getToc(), katalog.getVersion(), drds, tailoring.getPhasen());
        }

        String html = templateEngine.process(katalog.getVersion() + "/drd", parameter);
        Datei result = pdfEngine.process(docId, html, katalog.getVersion() + "/drd");

        log.traceExit("Finished creating DRD document {}", docId);

        return result;
    }


    void addDRD(@NonNull Kapitel<TailoringAnforderung> gruppe, String katalogVersion, Collection<DRDFragment> zeilen, Collection<Phase> phasen) {
        drdProvider.apply(gruppe, phasen)
            .keySet()
            .stream()
            .sorted(Comparator.comparing(DRD::getNummer))
            .map(drd -> DRDFragment.builder()
                .name(drd.getTitel())
                .nummer(drd.getNummer())
                .fragment("/" + katalogVersion + "/drd/drd-" + (drd.getNummer().contains(".") && drd.getNummer().charAt(0) == '0' ? drd.getNummer().substring(1) : drd.getNummer()))
                .build()
            )
            .forEachOrdered(zeilen::add);
    }
}
