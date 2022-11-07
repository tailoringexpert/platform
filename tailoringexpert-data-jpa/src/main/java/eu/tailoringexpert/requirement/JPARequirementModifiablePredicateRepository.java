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
package eu.tailoringexpert.requirement;

import eu.tailoringexpert.domain.TailoringState;
import eu.tailoringexpert.repository.ProjectRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.transaction.Transactional;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link RequirementServiceRepository}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
@Transactional
public class JPARequirementModifiablePredicateRepository implements RequirementModifiablePredicateRepository {

    @NonNull
    private ProjectRepository projectRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<TailoringState> getTailoringState(String project, String tailoring) {
        return ofNullable(projectRepository.findTailoringState(project, tailoring));
    }
}
