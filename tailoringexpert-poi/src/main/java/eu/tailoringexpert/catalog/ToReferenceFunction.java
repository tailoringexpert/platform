/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2024 Michael Bädorf and others
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

import eu.tailoringexpert.domain.Logo;
import eu.tailoringexpert.domain.Reference;
import eu.tailoringexpert.domain.Reference.ReferenceBuilder;

import java.util.function.BiFunction;

public class ToReferenceFunction implements BiFunction<String, Logo, Reference> {

    @Override
    public Reference apply(String ref, Logo logo) {
        Reference result = null;
        String reference = ref.trim();
        if (!reference.isBlank()) {
            ReferenceBuilder builder = Reference.builder();
            if (reference.contains("(mod)")) {
                builder.text(reference.substring(0, reference.indexOf("(mod)")).trim())
                    .changed(true);
            } else {
                builder.text(reference)
                    .changed(false);
            }
            builder.logo(logo);

            result = builder.build();

        }
        return result;
    }

}
