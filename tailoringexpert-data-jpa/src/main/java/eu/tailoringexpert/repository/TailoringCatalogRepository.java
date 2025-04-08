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
package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.TailoringCatalogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data access layer of {@link TailoringCatalogEntity}.
 *
 * @author Michael Bädorf
 */
public interface TailoringCatalogRepository extends JpaRepository<TailoringCatalogEntity, Long> {

    /**
     * Checks if base catalog of requested version already used in any tailoring catalog.
     *
     * @param version version of base catalog to check usage of
     * @return true, of base catalog is used
     */
    boolean existsByVersion(String version);
}
