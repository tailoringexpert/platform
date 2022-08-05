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
package de.baedorf.tailoringexpert.domain;

import org.springframework.http.MediaType;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.function.Function;

import static java.util.Locale.ROOT;
import static java.util.Map.ofEntries;
import static org.springframework.http.MediaType.valueOf;

public class MediaTypeProvider implements Function<String, MediaType> {
    public static final String FORM_DATA = "form-data";
    public static final String ATTACHMENT = "attachment";

    private static final Map<String, MediaType> contentTypes = ofEntries(
        new SimpleEntry<>("pdf", valueOf("application/pdf")),
        new SimpleEntry<>("xlsx", valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
        new SimpleEntry<>("zip", valueOf("application/zip")),
        new SimpleEntry<>("json", valueOf("application/json"))
    );

    @Override
    public MediaType apply(String type) {
        return contentTypes.get(type.toLowerCase(ROOT));
    }
}
