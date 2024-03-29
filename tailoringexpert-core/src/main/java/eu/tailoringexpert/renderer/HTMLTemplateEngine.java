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

import java.util.Map;

/**
 * Interface to be implemented for an HTML renderer engine.
 *
 * @author Michael Bädorf
 */
public interface HTMLTemplateEngine {

    /**
     * Create HTML string by using the provided template and variables.
     *
     * @param template  template to use
     * @param variables variables to use in template
     * @return generated HTML String
     */
    String process(String template, Map<String, Object> variables);

    /**
     * Convert a given (HTML) string to valid XHTML.
     *
     * @param text         text string
     * @param placeholders placeholders to replace in text
     * @return valid XHTML
     */
    String toXHTML(String text, Map<String, Object> placeholders);


}
