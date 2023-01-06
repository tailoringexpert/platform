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

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.DocumentSignature;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.nonNull;

/**
 * Create PDF requirement catalog file.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class TailoringCatalogPDFDocumentCreator implements DocumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    @NonNull
    private BiFunction<Chapter<TailoringRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    private static final String REFERENZ_LOGO_LINK = "<img src=\"%s\" alt=\"%s\"></img><br/>";

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(String docId,
                               Tailoring tailoring,
                               Map<String, Object> placeholders) {
        log.traceEntry("Start creating requirements document {}", docId);

        Map<String, Object> parameter = new HashMap<>(placeholders);
        parameter.put("catalogVersion", tailoring.getCatalog().getVersion());

        Collection<CatalogElement> requirements = new LinkedList<>();
        parameter.put("requirements", requirements);

        Collection<DRDElement> drds = new LinkedList<>();
        parameter.put("drds", drds);

        Map<String, String> bookmarks = new LinkedHashMap<>();
        parameter.put("bookmarks", bookmarks);

        tailoring.getCatalog().getToc().getChapters()
            .forEach(chapter -> {
                    bookmarks.put(chapter.getNumber(), chapter.getName());
                    addChapter(chapter, 1, requirements, placeholders);
                }
            );
        addDRD(tailoring.getCatalog().getToc(), drds, tailoring.getPhases());

        parameter.put("signatures", tailoring.getSignatures().stream()
            .sorted(comparingInt(DocumentSignature::getPosition))
            .toList());

        String html = templateEngine.process(tailoring.getCatalog().getVersion() + "/tailoringcatalog", parameter);
        File result = pdfEngine.process(docId, html, tailoring.getCatalog().getVersion() + "/tailoringcatalog");

        log.traceExit("Finished creating requirements document {}", docId);

        return result;
    }

    /**
     * Add chapter and all requirement to rows object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param level   chapter level
     * @param rows    collection to add elements to
     */
    void addChapter(Chapter<TailoringRequirement> chapter, int level, Collection<CatalogElement> rows, Map<String, Object> placeholders) {
        rows.add(CatalogElement.builder()
            .text(templateEngine.toXHTML(chapter.getNumber() + " " + chapter.getName(), emptyMap()))
            .chapter(chapter.getNumber())
            .applicable(true)
            .build());
        chapter.getRequirements()
            .forEach(requirement -> addRequirement(requirement, rows, placeholders));
        final AtomicInteger nextLevel = new AtomicInteger(level + 1);
        chapter.getChapters()
            .forEach(subChapter -> addChapter(subChapter, nextLevel.get(), rows, placeholders));
    }

    /**
     * Add a evaluated requirement to rows collection.
     *
     * @param requirement  requirement to build row object of
     * @param rows         collection to add to
     * @param placeholders placeholders to use for evaluation in requirement text
     */
    void addRequirement(TailoringRequirement requirement, Collection<CatalogElement> rows, Map<String, Object> placeholders) {
        StringBuilder referenzText = new StringBuilder();
        if (nonNull(requirement.getReference())) {
            if (nonNull(requirement.getReference().getLogo())) {
                String url = requirement.getReference().getLogo().getUrl();
                referenzText.append(format(REFERENZ_LOGO_LINK, url, requirement.getReference().getLogo().getName()));
            }
            referenzText.append(requirement.getReference().getText() + (requirement.getReference().getChanged().booleanValue() ? "(mod)" : ""));
        }

        rows.add(CatalogElement.builder()
            .applicable(requirement.getSelected().booleanValue())
            .reference(templateEngine.toXHTML(referenzText.toString(), emptyMap()))
            .position(templateEngine.toXHTML(requirement.getPosition(), emptyMap()))
            .text(templateEngine.toXHTML(requirement.getText(), placeholders))
            .chapter(null)
            .build());
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
}
