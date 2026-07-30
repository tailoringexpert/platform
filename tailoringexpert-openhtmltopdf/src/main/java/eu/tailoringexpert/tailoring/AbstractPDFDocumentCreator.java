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

import java.util.Map;

import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import eu.tailoringexpert.renderer.PDFEngine;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractPDFDocumentCreator {

    @NonNull
    private HTMLTemplateEngine templateEngine;

    @NonNull
    private PDFEngine pdfEngine;

    /**
     * Formats text with replaced placeholders as valid xhtml.
     *
     * @param text         text to format and replaced with placeholders
     * @param placeholders placeholders to use
     * @return formatted xhtml text
     */
    protected String toXhtml(String text, Map<String, Object> placeholders) {
        return templateEngine.toXHTML(text, placeholders);
    }

    /**
     * Create HTML string by using the provided template and variables.
     *
     * @param template  template to use
     * @param variables variables to use in template
     * @return generated HTML String
     */
    protected String toHtml(String template, Map<String, Object> parameter) {
        return templateEngine.process(template, parameter);
    }

    /**
     * Creates PDF using provided HTML String.
     *
     * @param docId      docid to use in PDF file
     * @param html       HTML to use as input
     * @param pathSuffix Path relarive to defined <strong>baseUri</strong>. Will be
     *                   used for relative addressing of images
     * @return Die erzeugte "PA" File
     */
    protected File toFile(String docId, String html, String pathSuffix) {
        return pdfEngine.process(docId, html, pathSuffix);
    }
}
