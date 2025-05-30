/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2025 Michael Bädorf and others
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
package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.ApplicableDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access layer of {@link ApplicableDocumentEntity}.
 *
 * @author Michael Bädorf
 */
public interface ApplicableDocumentRepository extends JpaRepository<ApplicableDocumentEntity, Long> {

    /**
     * Load a document.
     *
     * @param title of document to load
     * @return loaded document
     */
    ApplicableDocumentEntity findByTitleAndIssueAndRevision(String title, String issue, String revision);
}
