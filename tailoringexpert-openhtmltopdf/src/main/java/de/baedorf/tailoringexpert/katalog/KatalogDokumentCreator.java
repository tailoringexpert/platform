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
package de.baedorf.tailoringexpert.katalog;

import de.baedorf.tailoringexpert.domain.Datei;
import de.baedorf.tailoringexpert.domain.Identifikator;
import de.baedorf.tailoringexpert.domain.Kapitel;
import de.baedorf.tailoringexpert.domain.Katalog;
import de.baedorf.tailoringexpert.domain.KatalogAnforderung;
import de.baedorf.tailoringexpert.domain.Phase;
import de.baedorf.tailoringexpert.renderer.PDFEngine;
import de.baedorf.tailoringexpert.tailoring.DRDElement;
import de.baedorf.tailoringexpert.tailoring.KatalogElement;
import de.baedorf.tailoringexpert.renderer.HTMLTemplateEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static de.baedorf.tailoringexpert.domain.Phase.A;
import static de.baedorf.tailoringexpert.domain.Phase.B;
import static de.baedorf.tailoringexpert.domain.Phase.C;
import static de.baedorf.tailoringexpert.domain.Phase.D;
import static de.baedorf.tailoringexpert.domain.Phase.E;
import static de.baedorf.tailoringexpert.domain.Phase.F;
import static de.baedorf.tailoringexpert.domain.Phase.ZERO;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toCollection;

/**
 * Erzeugung eines PDF Anforderungkataloges.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class KatalogDokumentCreator implements DokumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    private static final String REFERENZ_LOGO_LINK = "<img src=\"%s\" alt=\"%s\"></img><br/>";

    /**
     * {@inheritDoc}
     */
    @Override
    public Datei createDokument(@NonNull String docId,
                                @NonNull Katalog<KatalogAnforderung> katalog,
                                @NonNull Map<String, String> platzhalter) {
        try {
            Map<String, Object> parameter = new HashMap<>();
            parameter.put("katalogVersion", katalog.getVersion());

            Collection<KatalogElement> anforderungen = new LinkedList<>();
            parameter.put("anforderungen", anforderungen);

            Collection<DRDElement> drds = new LinkedList<>();
            parameter.put("drds", drds);

            Map<String, String> bookmarks = new LinkedHashMap<>();
            parameter.put("bookmarks", bookmarks);

            parameter.put("phasen", Arrays.asList(ZERO, A, B, C, D, E, F));

            katalog.getToc().getKapitel()
                .forEach(gruppe -> {
                    bookmarks.put(gruppe.getNummer(), gruppe.getName());
                    addGruppe(gruppe, 1, anforderungen);
                });

            String html = templateEngine.process(katalog.getVersion() + "/katalog", parameter);

            return pdfEngine.process(docId, html, katalog.getVersion());
        } catch (Exception e) {
            log.catching(e);
        }
        return null;
    }


    void addGruppe(Kapitel<KatalogAnforderung> gruppe, int ebene, Collection<KatalogElement> zeilen) {
        zeilen.add(KatalogElement.builder()
            .text(templateEngine.toXHTML(gruppe.getNummer() + " " + gruppe.getName(), emptyMap()))
            .kapitel(gruppe.getNummer())
            .anwendbar(true)
            .build());
        gruppe.getAnforderungen()
            .forEach(anforderung -> addAnforderung(anforderung, zeilen, gruppe.getNummer()));
        final AtomicInteger naechsteEbene = new AtomicInteger(ebene + 1);
        if (nonNull(gruppe.getKapitel())) {
            gruppe.getKapitel()
                .forEach(subgroup -> addGruppe(subgroup, naechsteEbene.get(), zeilen));
        }

    }

    private String buildReferenzText(KatalogAnforderung anforderung) {
        StringBuilder referenzText = new StringBuilder();
        if (nonNull(anforderung.getReferenz())) {
            if (nonNull(anforderung.getReferenz().getLogo())) {
                String url = anforderung.getReferenz().getLogo().getUrl();
                referenzText.append(format(REFERENZ_LOGO_LINK, url, anforderung.getReferenz().getLogo().getName()));
            }
            referenzText.append(anforderung.getReferenz().getText() + (anforderung.getReferenz().getGeaendert().booleanValue() ? "(mod)" : ""));
        }
        return referenzText.toString();
    }

    private List<String> buildLimitierungen(Identifikator identifikator) {
        List<String> result = new ArrayList<>();
        if (identifikator.getLevel() > 0) {
            if (isNull(identifikator.getLimitierungen()) || identifikator.getLimitierungen().isEmpty()) {
                result.add(identifikator.getTyp() + identifikator.getLevel());
            } else {
                identifikator.getLimitierungen()
                    .stream()
                    .map(limitierung -> identifikator.getTyp() + identifikator.getLevel() + "(" + limitierung + ")")
                    .forEachOrdered(result::add);
            }
        }
        return result;
    }

    void addAnforderung(KatalogAnforderung anforderung, Collection<KatalogElement> zeilen, String kapitel) {
        String referenzText = buildReferenzText(anforderung);

        List<String> identifikatoren = new ArrayList<>();
        if (!anforderung.getIdentifikatoren().isEmpty()) {
            anforderung.getIdentifikatoren()
                .stream()
                .forEach(identifikator -> identifikatoren.addAll(buildLimitierungen(identifikator)));
        }

        Collection<String> phasen = new ArrayList<>();
        if (nonNull(anforderung.getPhasen())) {
            anforderung.getPhasen()
                .stream()
                .sorted(Comparator.comparing(Phase::ordinal))
                .map(Phase::getValue)
                .collect(toCollection(() -> phasen));
        }

        log.trace(kapitel + "." + anforderung.getPosition() + ": " + identifikatoren + ", " + phasen);
        zeilen.add(KatalogElement.builder()
            .phasen(phasen)
            .identifikatoren(identifikatoren)
            .referenz(templateEngine.toXHTML(referenzText, emptyMap()))
            .position(templateEngine.toXHTML(anforderung.getPosition(), emptyMap()))
            .text(templateEngine.toXHTML(anforderung.getText(), emptyMap()))
            .kapitel(null)
            .build());
    }


}
