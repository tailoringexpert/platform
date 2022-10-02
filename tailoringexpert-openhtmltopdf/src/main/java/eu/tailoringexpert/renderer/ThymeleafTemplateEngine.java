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
package eu.tailoringexpert.renderer;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implemenation of @see {@link HTMLTemplateEngine} using Thymeleaf and Jsoup.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class ThymeleafTemplateEngine implements HTMLTemplateEngine {

    @NonNull
    private ITemplateEngine templateEngine;

    /**
     * {@inheritDoc}
     */
    @Override
    public String process(String template, Map<String, Object> parameter) {
        return templateEngine.process(template, new Context(Locale.GERMAN, parameter));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toXHTML(String text, Map<String, String> placeholders) {
        AtomicReference<String> updatedText = new AtomicReference<>(text);
        placeholders.entrySet()
            .forEach(entry -> updatedText.set(updatedText.get().replace(entry.getKey(), entry.getValue())));
        Document document = Jsoup.parseBodyFragment(updatedText.get());
        document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        return document.body().html();
    }
}
