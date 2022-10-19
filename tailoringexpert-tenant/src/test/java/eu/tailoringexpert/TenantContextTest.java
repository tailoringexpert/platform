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

import java.lang.reflect.Field;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.AvoidAccessibilityAlteration"})
class TenantContextTest {

    @BeforeEach
    void beforeEach() throws Exception {
        Field field = TenantContext.class.getDeclaredField("registeredTenants");
        field.setAccessible(true);
        field.set(null, new HashSet<String>());
        field.setAccessible(false);

        TenantContext.setCurrentTenant(null);
    }

    @Test
    void registerTenant_TenantNull_TenantNotAdded() {
        // arrange

        // act
        TenantContext.registerTenant(null);

        // assert
        assertThat(TenantContext.getRegisteredTenants()).isEmpty();
    }


    @Test
    void registerTenant_TenantEmptyString_TenantNotAdded() {
        // arrange

        // act
        TenantContext.registerTenant(" ");

        // assert
        assertThat(TenantContext.getRegisteredTenants()).isEmpty();
    }

    @Test
    void registerTenant_TenantValid_TenantAdded() {
        // arrange

        // act
        TenantContext.registerTenant("TENANT");

        // assert
        assertThat(TenantContext.getRegisteredTenants()).hasSize(1);
    }

    @Test
    void getRegisteredTenants_NoTentanRegistered_EmptySet() {
        // arrange

        // act

        // assert
        assertThat(TenantContext.getRegisteredTenants()).isEmpty();
    }

    @Test
    void getRegisteredTenants_2TenantsRegistriered_SetContains2Tenants() {
        // arrange

        // act
        TenantContext.registerTenant("TENANT1");
        TenantContext.registerTenant("TENANT2");

        // assert
        assertThat(TenantContext.getRegisteredTenants()).hasSize(2);
        assertThat(TenantContext.getRegisteredTenants()).containsOnlyOnce("TENANT1", "TENANT2");
    }

    @Test
    void getRegisteredTenants_TenantRegisteredTwice_SetWithUniqueTenant() {
        // arrange

        // act
        TenantContext.registerTenant("TENANT1");
        TenantContext.registerTenant("TENANT1");

        // assert
        assertThat(TenantContext.getRegisteredTenants()).hasSize(1);
        assertThat(TenantContext.getRegisteredTenants()).containsOnlyOnce("TENANT1");
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