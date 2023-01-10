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

import java.util.Map;

/**
 * Proxy for providing tenant implementations of {@link HTMLTemplateEngine}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class TenantTemplateEngine implements HTMLTemplateEngine {

    @NonNull
    private final HTMLTemplateEngine templateEngine;

    @NonNull
    private RendererRequestConfigurationSupplier supplier;

    /**
     * {@inheritDoc}
     */

    @Override
    public String process(String template, Map<String, Object> parameter) {
        return templateEngine.process("/" + supplier.get().getId() + "/" + template, parameter);
    }

    @Override
    public String toXHTML(String text, Map<String, Object> placeholders) {
        return templateEngine.toXHTML(text, placeholders);
    }
}
