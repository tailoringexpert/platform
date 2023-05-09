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
package eu.tailoringexpert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TenantsTest {

    @Test
    void registerTenants_OneImplementationinFactory_MapWith1ImplementationReturned() {
        // arrange
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("DummyCacheManager", new DummyCacheManager());
        beanFactory.addBean("DummyClass", new DummyClass());

        // act
        Map<String, CacheManager> actual = Tenants.get(beanFactory, CacheManager.class);

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(1);
    }

    @Test
    void registerTenants_TwoImplementationinFactory_MapWith2ImplementationReturned() {
        // arrange
        StaticListableBeanFactory beanFactory = new StaticListableBeanFactory();
        beanFactory.addBean("DummyCacheManager", new DummyCacheManager());
        beanFactory.addBean("DummyClass", new DummyClass());
        beanFactory.addBean("Dummy2CacheManager", new Dummy2CacheManager());

        // act
        Map<String, CacheManager> actual = Tenants.get(beanFactory, CacheManager.class);

        // assert
        assertThat(actual)
            .isNotNull()
            .hasSize(2);
    }

    @Tenant("Dummy")
    static class DummyCacheManager implements CacheManager {

        @Override
        public Cache getCache(String name) {
            return null;
        }

        @Override
        public Collection<String> getCacheNames() {
            return null;
        }
    }

    @Tenant("Dummy")
    static class DummyClass implements Serializable {

    }

    @Tenant("Dummy2")
    static class Dummy2CacheManager implements CacheManager {

        @Override
        public Cache getCache(String name) {
            return null;
        }

        @Override
        public Collection<String> getCacheNames() {
            return null;
        }
    }
}
