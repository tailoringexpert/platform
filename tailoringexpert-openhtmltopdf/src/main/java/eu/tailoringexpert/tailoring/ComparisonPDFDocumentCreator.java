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
import eu.tailoringexpert.domain.File;
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

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

/**
 * Create PDF document containg differences of automatic and manual tailoring.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Log4j2
public class ComparisonPDFDocumentCreator implements DocumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(@NonNull String docId,
                               @NonNull Tailoring tailoring,
                               @NonNull Map<String, String> placeholders) {
        try {
            Map<String, Object> parameter = new HashMap<>(placeholders);

            Collection<ComparisionElement> requirements = new LinkedList<>();
            parameter.put("screeningsheet", tailoring.getScreeningSheet().getParameters());

            parameter.put("requirements", requirements);
            if (nonNull(tailoring.getCatalog().getToc())) {
                tailoring.getCatalog().getToc().getChapters()
                    .forEach(chapter -> addChapter(chapter, requirements));
            }

            String html = templateEngine.process(tailoring.getCatalog().getVersion() + "/comparision", parameter);

            return pdfEngine.process(docId, html, tailoring.getCatalog().getVersion());
        } catch (Exception e) {
            log.catching(e);
        }
        return null;
    }

    void addChapter(Chapter<TailoringRequirement> chapter, Collection<ComparisionElement> rows) {
        rows.add(ComparisionElement.builder()
            .section(templateEngine.toXHTML(chapter.getNumber(), emptyMap()))
            .title(templateEngine.toXHTML(chapter.getName(), emptyMap()))
            .build());
        chapter.getRequirements()
            .forEach(requirement -> addRequirement(requirement, rows));
        if (nonNull(chapter.getChapters())) {
            chapter.getChapters()
                .forEach(subChapter -> addChapter(subChapter, rows));
        }
    }

    void addRequirement(TailoringRequirement requirement, Collection<ComparisionElement> rows) {
        rows.add(ComparisionElement.builder()
            .section(templateEngine.toXHTML(requirement.getPosition(), emptyMap()))
            .selected(requirement.getSelected())
            .changed(nonNull(requirement.getSelectionChanged()))
            .changeDate(requirement.getSelectionChanged())
            .build());
    }
}
