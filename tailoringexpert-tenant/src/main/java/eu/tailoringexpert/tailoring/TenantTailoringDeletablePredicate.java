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

import eu.tailoringexpert.TenantContext;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static java.util.Objects.nonNull;

/**
 * Proxy for providing tenant implementations of {@link TailoringDeletablePredicate}.
 *
 * @author Michael Bädorf
 */
@RequiredArgsConstructor
public class TenantTailoringDeletablePredicate implements TailoringDeletablePredicate {

    @NonNull
    private Map<String, TailoringDeletablePredicate> tenantPredicate;

    @NonNull
    private TailoringDeletablePredicate defaultPredicate;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean test(String project, String tailoring) {
        return getTenantImplementation().test(project, tailoring);
    }

    private TailoringDeletablePredicate getTenantImplementation() {
        TailoringDeletablePredicate result = tenantPredicate.get(TenantContext.getCurrentTenant());
        return nonNull(result) ? result : defaultPredicate;
    }
}
