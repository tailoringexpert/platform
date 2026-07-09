/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Creates a PDF document containing text or applicability diferences between
 * two tailoring.
 * <p>
 * A HTML template named <code>tailoringdiffs.html</code> must be available in
 * the template folder of the tailoring version.
 * +
 * 
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class TailoringsDiffPDFDocumentCreator implements DiffDocumentCreator {

    @NonNull
    BiFunction<TailoringRequirement, TailoringRequirement, Optional<TailoringRequirementDiff>> diffProvider;

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    @Override
    public File createDocument(String docId, Tailoring base, Tailoring compare, Map<String, Object> placeholders) {
        log.traceEntry(base.getCatalog().getVersion());
        Map<String, Object> parameter = new HashMap<>(placeholders);

        Map<String, List<TailoringRequirementDiff>> diffs = new LinkedHashMap<>();
        parameter.put("diffs", diffs);

        apply(base.getCatalog().getToc(), compare.getCatalog().getToc(), diffs);
        diffs.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        // dokument generieren
        String html = templateEngine.process(base.getCatalog().getVersion() + "/tailoringdiffs", parameter);

        File result = pdfEngine.process("docId", html, base.getCatalog().getVersion() + "/tailoringdiffs");

        log.traceExit();
        return result;
    }

    void apply(Chapter<TailoringRequirement> base,
            Chapter<TailoringRequirement> compare,
            Map<String, List<TailoringRequirementDiff>> diffs) {
        log.traceEntry(() -> base.getNumber());

        String key = templateEngine.toXHTML(base.getNumber() + " " + base.getName(), Map.of());
        List<TailoringRequirementDiff> requirements = diffs.computeIfAbsent(key, k -> new ArrayList<>());

        base.getRequirements()
                .stream()
                .map(bReq -> {
                    TailoringRequirement cReq = ofNullable(compare)
                            .flatMap(c -> c.getRequirement(bReq.getPosition()))
                            .orElse(null);
                    return diffProvider.apply(bReq, cReq);
                })
                .flatMap(Optional::stream)
                .forEachOrdered(requirements::add);

        if (nonNull(base.getChapters())) {
            base.getChapters().forEach(subChapter -> {
                Chapter<TailoringRequirement> subCompare = Optional.ofNullable(compare)
                        .map(c -> c.getChapter(subChapter.getNumber()))
                        .orElse(null);
                apply(subChapter, subCompare, diffs);
            });
        }

        log.traceExit();
    }

}
