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

import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static java.util.Collections.emptyMap;

/**
 * Create PDF Compliance Matrix.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
public class CMPDFDocumentCreator implements DocumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    @NonNull
    private BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(String docId,
                               Tailoring tailoring,
                               Map<String, Object> placeholders) {
        log.traceEntry(() -> docId, () -> tailoring.getCatalog().getVersion(), () -> placeholders);

        Map<String, Object> parameter = new HashMap<>(placeholders);
        parameter.put("signatures", tailoring.getSignatures());

        Collection<CMElement> chapters = new LinkedList<>();
        parameter.put("chapters", chapters);

        Collection<DRDElement> drds = new LinkedList<>();
        parameter.put("drds", drds);

        Catalog<TailoringRequirement> catalog = tailoring.getCatalog();
        catalog.getToc().getChapters()
            .forEach(chapter -> addChapter(chapter, 1, chapters, placeholders));
        addDRD(catalog.getToc(), drds, tailoring.getPhases());

        String html = templateEngine.process(catalog.getVersion() + "/cm", parameter);
        File result = pdfEngine.process(docId, html, tailoring.getCatalog().getVersion());

        log.traceExit();
        return result;
    }

    /**
     * Add chapter to rows object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param level   chapter level
     * @param rows    collection to add elements to
     */
    void addChapter(Chapter<TailoringRequirement> chapter, int level, Collection<CMElement> rows, Map<String, Object> placeholders) {
        rows.add(CMElement.builder()
            .level(level)
            .number(xhtml(chapter.getNumber(), emptyMap()))
            .name(xhtml(chapter.getName(), placeholders))
            .requirement(false)
            .build());
        AtomicInteger nextLevel = new AtomicInteger(level + 1);
        chapter.getChapters()
            .forEach(subChapter -> addChapter(subChapter, nextLevel.get(), rows, placeholders));
        addRequirements(chapter.getRequirements(), nextLevel.get(), rows, placeholders);
    }

    /**
     * Hook fpr adding also requirements to CM instead of only adding chapters.<p>
     * The hook itself doesn't add requirements to CM.
     *
     * @param requirements requirements to add
     * @param level        level of requirements
     * @param rows         collection to add elements to
     * @param placeholders placeholder to use
     */
    protected void addRequirements(Collection<TailoringRequirement> requirements,
                                   int level,
                                   Collection<CMElement> rows,
                                   Map<String, Object> placeholders) {
        requirements.forEach(requirement -> rows.add(CMElement.builder()
            .level(level)
            .number(xhtml(requirement.getPosition(), emptyMap()))
            .name(xhtml(requirement.getText(), placeholders))
            .requirement(true)
            .build())
        );
        // noop hook for adding requirements to cm
    }

    /**
     * Evaluate all applicable DRD in chapter for given phases and add them to row object.
     *
     * @param chapter chapter to retrieve requirements DRDs of
     * @param rows    object to add DRDs to
     * @param phases  phase of tailoring to use of applicabilty check
     */
    void addDRD(Chapter<TailoringRequirement> chapter, Collection<DRDElement> rows, Collection<Phase> phases) {
        drdProvider.apply(chapter, phases)
            .entrySet()
            .forEach(entry -> rows.add(DRDElement.builder()
                .title(entry.getKey().getTitle())
                .deliveryDate(entry.getKey().getDeliveryDate())
                .requirements(entry.getValue())
                .number(entry.getKey().getNumber())
                .action(entry.getKey().getAction())
                .build()));
    }

    /**
     * Formats text with replaced placeholders as valid xhtml.
     *
     * @param text         text to format and replaced with placeholders
     * @param placeholders placeholders to use
     * @return formatted xhtml text
     */
    private String xhtml(String text, Map<String, Object> placeholders) {
        return templateEngine.toXHTML(text, emptyMap());
    }
}
