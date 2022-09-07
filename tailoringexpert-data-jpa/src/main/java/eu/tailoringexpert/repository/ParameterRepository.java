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
package eu.tailoringexpert.repository;

import eu.tailoringexpert.domain.ParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

/**
 * Spring Data access layer of {@link ParameterEntity}.
 *
 * @author Michael Bädorf
 */
public interface ParameterRepository extends JpaRepository<ParameterEntity, Long> {
    /**
     * Load parameter for all requested names.
     *
     * @param names names of parameter to load
     * @return loaded parameters
     */
    Collection<ParameterEntity> findByNameIn(Collection<String> names);
}
