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
package eu.tailoringexpert.renderer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import eu.tailoringexpert.domain.CatalogElement;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Function to filter elements of CatalogElement collection and all its
 * subchapters.
 * 
 * @author Michael Bädorf
 */
@Log4j2
public class CatalogElementFilter {

    public Collection<CatalogElement> apply(List<CatalogElement> elements, @NonNull String chapter) {
        log.traceEntry(() -> chapter);

        Collection<CatalogElement> result = new LinkedList<>();
        if (isNull(elements)) {
            log.info("Null requirements, returning empty list");
            log.traceExit();
            return result;
        }

        int tokens = chapter.split("\\.").length;

        // such start
        String currentChapter = chapter;
        Iterator<CatalogElement> iterator = elements.iterator();
        while (iterator.hasNext()) {
            CatalogElement element = iterator.next();
            currentChapter = nonNull(element.getChapter()) ? element.getChapter() : currentChapter;
            if (currentChapter.startsWith(chapter) && currentChapter.split("\\.").length >= tokens) {
                result.add(element);
            }
        }

        log.traceExit("filtered elements {} ", result.size());
        return result;
    }

}
