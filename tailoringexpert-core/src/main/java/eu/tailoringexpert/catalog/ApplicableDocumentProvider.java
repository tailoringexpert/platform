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
import eu.tailoringexpert.domain.Document;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Function for determinating all relevant DRDs of a tailoring.<p>
 * Revelant are DRDs, which are referenced in requirements and their delivery date are within the phases.
 *
 * @author Michael Bädorf
 */
@Log4j2
@RequiredArgsConstructor
public class ApplicableDocumentProvider implements Function<Catalog<BaseRequirement>, Collection<Document>> {

    @NonNull
    private Comparator<Document> numberComparator;

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Document> apply(Catalog<BaseRequirement> catalog) {
        log.traceEntry(catalog::getVersion);

        Collection<Document> result = new TreeSet<>(numberComparator);

        catalog.getToc().allRequirements()
            .filter(BaseRequirement::hasApplicableDocument)
            .map(BaseRequirement::getApplicableDocuments)
            .flatMap(Collection::stream)
            .forEachOrdered(result::add);

        return log.traceExit(result);
    }

}
