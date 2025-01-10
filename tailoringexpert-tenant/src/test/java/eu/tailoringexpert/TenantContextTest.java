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
package eu.tailoringexpert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.AvoidAccessibilityAlteration"})
class TenantContextTest {

    @BeforeEach
    void beforeEach() throws Exception {
        Field field = TenantContext.class.getDeclaredField("registeredTenants");
        field.setAccessible(true);
        field.set(null, new HashMap<String, String>());
        field.setAccessible(false);

        TenantContext.setCurrentTenant(null);
    }

    @Test
    void registerTenant_TenantIdNull_TenantNotAdded() {
        // arrange

        // act
        TenantContext.registerTenant(null, null);

        // assert
        assertThat(TenantContext.getRegisteredTenants()).isEmpty();
    }


    @Test
    void registerTenant_TenantEmptyString_TenantNotAdded() {
        // arrange

        // act
        TenantContext.registerTenant(" ", null);

        // assert
        assertThat(TenantContext.getRegisteredTenants()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource
    @SuppressWarnings({"java:S1144"})
    void registerTenant(String id, String name, String expected) { // NOPMD - suppressed UnusedPrivateMethod - ParameterizedTest
        // arrange

        // act
        TenantContext.registerTenant(id, name);

        // assert
        assertThat(TenantContext.getRegisteredTenants()).hasSize(1);
        assertThat(TenantContext.getRegisteredTenants()).containsEntry(id, expected);
    }

    @SuppressWarnings({"java:S1144"})
    private static Stream<Arguments> registerTenant() { // NOPMD - suppressed UnusedPrivateMethod - Used by parameterized test registerTenant
        return Stream.of(
            Arguments.of("TENANT", null, "TENANT"),
            Arguments.of("TENANT", " ", "TENANT"),
            Arguments.of("TENANT ", "Plattform", "Plattform")
        );
    }

    @Test
    void getRegisteredTenants_NoTenantRegistered_EmptySet() {
        // arrange

        // act

        // assert
        assertThat(TenantContext.getRegisteredTenants()).isEmpty();
    }

    @Test
    void getRegisteredTenants_2TenantsRegistriered_SetContains2Tenants() {
        // arrange

        // act
        TenantContext.registerTenant("TENANT1", "Tenant 1");
        TenantContext.registerTenant("TENANT2", "Tenant 2");

        // assert
        assertThat(TenantContext.getRegisteredTenants()).hasSize(2);
        assertThat(TenantContext.getRegisteredTenants()).containsOnlyKeys("TENANT1", "TENANT2");
    }

    @Test
    void getRegisteredTenants_TenantRegisteredTwice_SetWithUniqueTenant() {
        // arrange

        // act
        TenantContext.registerTenant("TENANT1", "Tenant 1");
        TenantContext.registerTenant("TENANT1", "Tenant 2");

        // assert
        assertThat(TenantContext.getRegisteredTenants()).hasSize(1);
        assertThat(TenantContext.getRegisteredTenants()).containsKeys("TENANT1");
    }

    @Test
    void setCurrentTenant_TenantNotSet_CurrentTenantNull() {
        // arrange

        // act

        // assert
        assertThat(TenantContext.getCurrentTenant()).isNull();
    }

    @Test
    void setCurrentTenant_TenantNull_CurrentTenantNull() {
        // arrange

        // act
        TenantContext.setCurrentTenant(null);

        // assert
        assertThat(TenantContext.getCurrentTenant()).isNull();
    }

    @Test
    void setCurrentTenant_TenantSet_TenantIsCurrentTenant() {
        // arrange

        // act
        TenantContext.setCurrentTenant("TENANT");

        // assert
        assertThat(TenantContext.getCurrentTenant()).isEqualTo("TENANT");
    }
}
