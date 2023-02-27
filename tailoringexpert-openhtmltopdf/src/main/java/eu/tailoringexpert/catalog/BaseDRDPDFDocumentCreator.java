/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2023 Michael Bädorf and others
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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.renderer.DRDFragment;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.Comparator.comparing;

/**
 * Create a base PDF with all in base catalog defined catalog.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class BaseDRDPDFDocumentCreator implements DocumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    @NonNull
    private Function<Chapter<BaseRequirement>, Set<DRD>> drdProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(String docId, Catalog<BaseRequirement> catalog, Map<String, String> placeholders) {
        log.traceEntry(() -> docId, catalog::getVersion, () -> placeholders);

        Map<String, Object> parameter = new HashMap<>(placeholders);
        parameter.put("catalogVersion", catalog.getVersion());

        Collection<DRDFragment> drds = new LinkedList<>();
        parameter.put("drds", drds);

        addDRD(catalog.getToc(), catalog.getVersion(), drds);

        String html = templateEngine.process(catalog.getVersion() + "/basedrd", parameter);
        File result = pdfEngine.process(docId, html, catalog.getVersion() + "/drd");

        log.traceExit();
        return result;
    }

    /**
     * @param chapter
     * @param catalogVersion catalog version used for constructing DRD fragment
     * @param rows           collection to add drd fragments to
     */
    void addDRD(Chapter<BaseRequirement> chapter, String catalogVersion, Collection<DRDFragment> rows) {
        drdProvider.apply(chapter)
            .stream()
            .sorted(comparing(DRD::getNumber))
            .map(drd -> DRDFragment.builder()
                .name(drd.getTitle())
                .number(drd.getNumber())
                .fragment(catalogVersion + "/drd/drd-" + drd.getNumber())
                .build())
            .forEachOrdered(rows::add);
    }
}
