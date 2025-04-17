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

import eu.tailoringexpert.domain.DocumentEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Data access layer of {@link DocumentEntity}.
 *
 * @author Michael Bädorf
 */
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    String CACHE_DOCUMENT = "DocumentRepository#Title";

    /**
     * Load a document.
     *
     * @param title of document to load
     * @return loaded document
     */
    @Cacheable(CACHE_DOCUMENT)
    @Transactional(readOnly = true)
    DocumentEntity findByTitle(String title);
}
