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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TenantTailoringDeletablePredicateTest {

    TailoringDeletablePredicate defaultPredicate;

    TailoringDeletablePredicate tenantPredicate;

    TenantTailoringDeletablePredicate predicate;

    @BeforeEach
    void beforeEach() {
        this.defaultPredicate = mock(TailoringDeletablePredicate.class);
        this.tenantPredicate = mock(TailoringDeletablePredicate.class);
        this.predicate = new TenantTailoringDeletablePredicate(
            Map.of("PLATTFORM", tenantPredicate),
            this.defaultPredicate
        );
    }

    @Test
    void test_TenantNotExists_DefaultPredicateCalled() {
        // arrange
        TenantContext.setCurrentTenant("INVALID");

        // act
        predicate.test("SAMPLE", "master");

        // assert
        verify(defaultPredicate, times(1)).test("SAMPLE", "master");
        verify(tenantPredicate, times(0)).test("SAMPLE", "master");
    }

    @Test
    void test_TenantExists_TenantPredicateCalled() {
        // arrange
        TenantContext.setCurrentTenant("PLATTFORM");

        // act
        predicate.test("SAMPLE", "master");

        // assert
        verify(defaultPredicate, times(0)).test("SAMPLE", "master");
        verify(tenantPredicate, times(1)).test("SAMPLE", "master");
    }
}
