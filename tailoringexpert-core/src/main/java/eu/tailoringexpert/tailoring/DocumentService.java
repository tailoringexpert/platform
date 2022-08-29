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

import eu.tailoringexpert.TenantInterface;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/**
 * Interface for providing generated document files of tailorings.
 * This interface has to be implemented by each tenant.
 *
 * @author Michael Bädorf
 */
@TenantInterface
public interface DocumentService {

    /**
     * Create a base requirements document.
     *
     * @param tailoring         Data to create document of
     * @param creationTimestamp timestamp of document creation
     * @return created document file
     */
    Optional<File> createRequirementDocument(Tailoring tailoring, LocalDateTime creationTimestamp);

    /**
     * Create a comparison document of differences between automatic and manual tailored requirements.
     *
     * @param tailoring         Data to create document of
     * @param creationTimestamp timestamp of document creation
     * @return created document file
     */
    Optional<File> createComparisonDocument(Tailoring tailoring, LocalDateTime creationTimestamp);

    /**
     * Creates all documents of a tailoring.
     *
     * @param tailoring         Data to create document of
     * @param creationTimestamp timestamp of document creation
     * @return created document {@code zip-file}
     */
    Collection<File> createAll(Tailoring tailoring, LocalDateTime creationTimestamp);
}
