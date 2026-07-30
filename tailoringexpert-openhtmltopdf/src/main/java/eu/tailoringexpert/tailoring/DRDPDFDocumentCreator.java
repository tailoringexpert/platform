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

import static java.util.Comparator.comparing;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.DRDFragment;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Create a DRD PDF file.
 *
 * @author Michael Bädorf
 */
@Log4j2
public class DRDPDFDocumentCreator extends AbstractPDFDocumentCreator implements DocumentCreator {

    @NonNull
    private BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    public DRDPDFDocumentCreator(
            BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider,
            HTMLTemplateEngine templateEngine,
            PDFEngine pdfEngine) {
        super(templateEngine, pdfEngine);
        this.drdProvider = drdProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(String docId,
            Tailoring tailoring,
            Map<String, Object> placeholders) {
        log.traceEntry(() -> docId, () -> tailoring.getCatalog().getVersion(), () -> placeholders);

        Map<String, Object> parameter = new HashMap<>(placeholders);
        parameter.put("catalogVersion", tailoring.getCatalog().getVersion());

        parameter.put("issue", tailoring.getIssue());

        Collection<DRDFragment> drds = new LinkedList<>();
        parameter.put("drds", drds);

        Catalog<TailoringRequirement> catalog = tailoring.getCatalog();
        addDRD(catalog.getToc(), catalog.getVersion(), drds, tailoring.getPhases());

        String html = toHtml(catalog.getVersion() + "/drd", parameter);
        File result = toFile(docId, html, tailoring.getCatalog().getVersion() + "/drd");

        log.traceExit();
        return result;
    }

    /**
     * @param chapter
     * @param catalogVersion catalog version used for constructing DRD fragment
     * @param rows           collection to add drd fragments to
     * @param phases         phase of tailoring to use of applicabilty check
     */
    void addDRD(Chapter<TailoringRequirement> chapter, String catalogVersion, Collection<DRDFragment> rows,
            Collection<Phase> phases) {
        drdProvider.apply(chapter, phases)
                .keySet()
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
