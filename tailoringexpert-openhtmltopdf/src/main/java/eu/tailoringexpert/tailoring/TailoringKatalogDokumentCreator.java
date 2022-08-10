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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.Datei;
import eu.tailoringexpert.domain.DokumentZeichnung;
import eu.tailoringexpert.domain.Kapitel;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringAnforderung;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.nonNull;

/**
 * Erzeugung eines PDF Anforderungkataloges.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class TailoringKatalogDokumentCreator implements DokumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    @NonNull
    private BiFunction<Kapitel<TailoringAnforderung>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    private Comparator<DokumentZeichnung> zeichnungsComparator =
        comparingInt(DokumentZeichnung::getPosition);

    private static final String REFERENZ_LOGO_LINK = "<img src=\"%s\" alt=\"%s\"></img><br/>";

    /**
     * {@inheritDoc}
     */
    @Override
    public Datei createDokument(String docId,
                                Tailoring tailoring,
                                Map<String, String> platzhalter) {
        log.traceEntry("Start creating requirements document {}", docId);

        Map<String, Object> parameter = new HashMap<>(platzhalter);
        parameter.put("katalogVersion", tailoring.getKatalog().getVersion());

        Collection<KatalogElement> anforderungen = new LinkedList<>();
        parameter.put("anforderungen", anforderungen);

        Collection<DRDElement> drds = new LinkedList<>();
        parameter.put("drds", drds);

        Map<String, String> bookmarks = new LinkedHashMap<>();
        parameter.put("bookmarks", bookmarks);

        if (nonNull(tailoring.getKatalog().getToc())) {
            tailoring.getKatalog().getToc().getKapitel()
                .forEach(gruppe -> {
                        bookmarks.put(gruppe.getNummer(), gruppe.getName());
                        addGruppe(gruppe, 1, anforderungen, platzhalter);
                    }
                );

            addDRD(tailoring.getKatalog().getToc(), drds, tailoring.getPhasen());
        }

        parameter.put("zeichnungen", tailoring.getZeichnungen().stream()
            .sorted(comparingInt(DokumentZeichnung::getPosition))
            .collect(Collectors.toList()));

        String html = templateEngine.process(tailoring.getKatalog().getVersion() + "/tailoringkatalog", parameter);
        Datei result = pdfEngine.process(docId, html, tailoring.getKatalog().getVersion() + "/katalog");

        log.traceExit("Finished creating requirements document {}", docId);

        return result;

    }


    void addGruppe(Kapitel<TailoringAnforderung> gruppe, int ebene, Collection<KatalogElement> zeilen, Map<String, String> platzhalter) {
        zeilen.add(KatalogElement.builder()
            .text(templateEngine.toXHTML(gruppe.getNummer() + " " + gruppe.getName(), emptyMap()))
            .kapitel(gruppe.getNummer())
            .anwendbar(true)
            .build());
        gruppe.getAnforderungen()
            .forEach(anforderung -> addAnforderung(anforderung, zeilen, platzhalter));
        final AtomicInteger naechsteEbene = new AtomicInteger(ebene + 1);
        if (nonNull(gruppe.getKapitel())) {
            gruppe.getKapitel()
                .forEach(subgroup -> addGruppe(subgroup, naechsteEbene.get(), zeilen, platzhalter));
        }

    }

    void addAnforderung(TailoringAnforderung anforderung, Collection<KatalogElement> zeilen, Map<String, String> platzhalter) {
        StringBuilder referenzText = new StringBuilder();
        if (nonNull(anforderung.getReferenz())) {
            if (nonNull(anforderung.getReferenz().getLogo())) {
                String url = anforderung.getReferenz().getLogo().getUrl();
                referenzText.append(format(REFERENZ_LOGO_LINK, url, anforderung.getReferenz().getLogo().getName()));
            }
            referenzText.append(anforderung.getReferenz().getText() + (anforderung.getReferenz().getGeaendert().booleanValue() ? "(mod)" : ""));
        }

        zeilen.add(KatalogElement.builder()
            .anwendbar(anforderung.getAusgewaehlt().booleanValue())
            .referenz(templateEngine.toXHTML(referenzText.toString(), emptyMap()))
            .position(templateEngine.toXHTML(anforderung.getPosition(), emptyMap()))
            .text(templateEngine.toXHTML(anforderung.getText(), platzhalter))
            .kapitel(null)
            .build());
    }

    void addDRD(@NonNull Kapitel<TailoringAnforderung> gruppe, Collection<DRDElement> zeilen, Collection<Phase> phasen) {
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
