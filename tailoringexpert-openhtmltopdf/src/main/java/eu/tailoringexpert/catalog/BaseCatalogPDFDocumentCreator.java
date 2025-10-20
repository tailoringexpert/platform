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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.BaseRequirement;
import eu.tailoringexpert.domain.Catalog;
import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.DRD;
import eu.tailoringexpert.domain.DRDElement;
import eu.tailoringexpert.domain.Document;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Identifier;
import eu.tailoringexpert.domain.Phase;
import eu.tailoringexpert.renderer.PDFEngine;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static eu.tailoringexpert.domain.Phase.A;
import static eu.tailoringexpert.domain.Phase.B;
import static eu.tailoringexpert.domain.Phase.C;
import static eu.tailoringexpert.domain.Phase.D;
import static eu.tailoringexpert.domain.Phase.E;
import static eu.tailoringexpert.domain.Phase.F;
import static eu.tailoringexpert.domain.Phase.ZERO;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.List.of;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toCollection;

/**
 * Create a base catalog PDF file.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class BaseCatalogPDFDocumentCreator implements DocumentCreator {

    @NonNull
    private Function<Catalog<BaseRequirement>, Collection<Document>> applicableDocumentProvider;

    @NonNull
    private BiFunction<Chapter<BaseRequirement>, Collection<Phase>, Map<DRD, Set<String>>> drdProvider;

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    private static final String REFERENZ_LOGO_LINK = "<img src=\"%s\" alt=\"%s\"></img><br/>";

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(@NonNull String docId,
                               @NonNull Catalog<BaseRequirement> catalog,
                               @NonNull Map<String, Object> placeholders) {
        log.traceEntry(() -> docId, catalog::getVersion, () -> placeholders);

        try {
            Map<String, Object> parameter = new HashMap<>(placeholders);
            parameter.put("catalogVersion", catalog.getVersion());

            parameter.put("applicableDocuments", applicableDocumentProvider.apply(catalog));

            Collection<BaseCatalogElement> requirements = new LinkedList<>();
            parameter.put("requirements", requirements);

            Collection<DRDElement> drds = new LinkedList<>();
            parameter.put("drds", drds);

            Map<String, String> bookmarks = new LinkedHashMap<>();
            parameter.put("bookmarks", bookmarks);

            List<Phase> phases = of(ZERO, A, B, C, D, E, F);
            parameter.put("phases", phases);

            catalog.getToc().getChapters()
                .forEach(chapter -> {
                    bookmarks.put(chapter.getNumber(), chapter.getName());
                    addChapter(chapter, 1, requirements);
                });
            addDRD(catalog.getToc(), drds, phases);

            String html = templateEngine.process(catalog.getVersion() + "/basecatalog", parameter);
            File result = pdfEngine.process(docId, html, catalog.getVersion() + "/catalog");

            log.traceExit();
            return result;
        } catch (Exception e) {
            log.catching(e);
        }
        log.traceExit();
        return null;
    }


    /**
     * Add chapter and all requirement to rows object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param level   chapter level
     * @param rows    collection to add elements to
     */
    void addChapter(Chapter<BaseRequirement> chapter, int level, Collection<BaseCatalogElement> rows) {
        rows.add(BaseCatalogElement.builder()
            .text(templateEngine.toXHTML(chapter.getNumber() + " " + chapter.getName(), emptyMap()))
            .chapter(chapter.getNumber())
                .level(level)
            .build());
        chapter.getRequirements()
            .forEach(requirement -> addRequirement(requirement, rows));
        final AtomicInteger nextLevel = new AtomicInteger(level + 1);
        chapter.getChapters()
            .forEach(subChapter -> addChapter(subChapter, nextLevel.get(), rows));
    }

    /**
     * Builds text of refernce origin.
     *
     * @param requirement requirment to create refernence of
     * @return created reference text
     */
    private String buildReferenceText(BaseRequirement requirement) {
        StringBuilder referenceText = new StringBuilder();
        if (nonNull(requirement.getReference())) {
            if (nonNull(requirement.getReference().getLogo())) {
                String url = requirement.getReference().getLogo().getUrl();
                referenceText.append(format(REFERENZ_LOGO_LINK, url, requirement.getReference().getLogo().getName()));
            }
            referenceText.append(requirement.getReference().getText() + (requirement.getReference().getChanged().booleanValue() ? "(mod)" : ""));
        }
        return referenceText.toString();
    }

    /**
     * Builds List of limitations.
     *
     * @param identifier identifier to create limitation strings of
     * @return list of extracted limitations
     */
    private List<String> buildLimitations(Identifier identifier) {
        List<String> result = new ArrayList<>();
        if (identifier.getLevel() > 0) {
            if (!identifier.hasLimitations()) {
                result.add(identifier.getType() + identifier.getLevel());
            } else {
                identifier.getLimitations()
                    .stream()
                    .map(limitation -> identifier.getType() + identifier.getLevel() + "(" + limitation + ")")
                    .forEachOrdered(result::add);
            }
        }
        return result;
    }

    /**
     * Add a evaluated requirene to rows collection.
     *
     * @param requirement requirement to build row object of
     * @param rows        collection to add to
     */
    void addRequirement(BaseRequirement requirement, Collection<BaseCatalogElement> rows) {
        BaseCatalogElement.BaseCatalogElementBuilder builder = BaseCatalogElement.builder();

        if (nonNull(requirement.getReference())) {
            builder.reference(templateEngine.toXHTML(requirement.getReference().getText() + (requirement.getReference().getChanged().booleanValue() ? "(mod)" : ""), emptyMap()));
            if (nonNull(requirement.getReference().getLogo())) {
                builder.logo(requirement.getReference().getLogo().getUrl());
            }
        }

        List<String> identifiers = new ArrayList<>();
        if (!requirement.getIdentifiers().isEmpty()) {
            requirement.getIdentifiers()
                .stream()
                .forEach(identifier -> identifiers.addAll(buildLimitations(identifier)));
        }

        Collection<String> phases = new ArrayList<>();
        if (nonNull(requirement.getPhases())) {
            requirement.getPhases()
                .stream()
                .sorted(Comparator.comparing(Phase::ordinal))
                .map(Phase::getValue)
                .collect(toCollection(() -> phases));
        }

        rows.add(builder
            .phases(phases)
            .identifiers(identifiers)
            .position(templateEngine.toXHTML(requirement.getPosition(), emptyMap()))
            .text(templateEngine.toXHTML(requirement.getText(), emptyMap()))
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
    void addDRD(Chapter<BaseRequirement> chapter, Collection<DRDElement> rows, Collection<Phase> phases) {
        drdProvider.apply(chapter, phases)
            .entrySet()
            .forEach(entry -> rows.add(DRDElement.builder()
                .title(entry.getKey().getTitle())
                .deliveryDate(entry.getKey().getDeliveryDate())
                .requirements(entry.getValue())
                .number(entry.getKey().getNumber())
                .action(entry.getKey().getAction())
                .subtitle(entry.getKey().getSubtitle())
                .build()));
    }
}
