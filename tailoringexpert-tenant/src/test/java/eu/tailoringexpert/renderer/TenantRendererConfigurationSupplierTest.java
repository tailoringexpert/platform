/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2023 Michael Bädorf and others
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
package eu.tailoringexpert.renderer;

import eu.tailoringexpert.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.AvoidAccessibilityAlteration"})
class TenantRendererConfigurationSupplierTest {

    TenantRendererConfigurationSupplier supplier;

    @BeforeEach
    void beforeEach() throws Exception {
        this.supplier = new TenantRendererConfigurationSupplier("src/test/resources");

        Field field = TenantContext.class.getDeclaredField("registeredTenants");
        field.setAccessible(true);
        field.set(null, new HashMap<String, String>());
        field.setAccessible(false);
        TenantContext.setCurrentTenant(null);
    }

    @Test
    void get_NoTenantIdGiven() {
        // arrange

        // act
        RendererRequestConfiguration actual = supplier.get();

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNull();
        assertThat(actual.getName()).isNull();
        assertThat(actual.getTemplateRoot()).isEqualTo("src/test/resources");
        assertThat(actual.getFragmentPrefix()).isEmpty();
    }

    @Test
    void get_TenantIdGivenTenantNotRegistered() {
        // arrange
        TenantContext.setCurrentTenant("ut");

        // act
        RendererRequestConfiguration actual = supplier.get();

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getId()).isEqualTo("ut");
        assertThat(actual.getName()).isNull();
        assertThat(actual.getTemplateRoot()).isEqualTo("src/test/resources/ut/");
        assertThat(actual.getFragmentPrefix()).isEqualTo("/ut/");
    }

    @Test
    void get_TenantIdGivenTenantRegistered() {
        // arrange
        TenantContext.registerTenant("ut", "unittest");
        TenantContext.setCurrentTenant("ut");

        // act
        RendererRequestConfiguration actual = supplier.get();

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getId()).isEqualTo("ut");
        assertThat(actual.getName()).isNotNull();
        assertThat(actual.getName()).isEqualTo("unittest");
        assertThat(actual.getTemplateRoot()).isEqualTo("src/test/resources/ut/");
        assertThat(actual.getFragmentPrefix()).isEqualTo("/ut/");
    }

}
