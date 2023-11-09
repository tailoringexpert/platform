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
import org.mockito.MockedStatic;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class TenantCacheManagerTest {

    @Test
    void getCache_CacheNotExists_NoOpCacheReturned() {
        // arrange
        TenantCacheManager cacheManager = new TenantCacheManager(new HashMap<>());

        // act
        Cache actual;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(() -> TenantContext.getCurrentTenant()).thenReturn("anyTenant");
            actual = cacheManager.getCache("anyTenant");
        }

        // assert
        assertThat(actual).isInstanceOf(NoOpCache.class);
    }

    @Test
    void getCache_CacheExists_CacheReturned() {
        // arrange
        Map<String, CacheManager> cacheManagers = Map.of("anyTenant", new CacheManager() {
            @Override
            public Cache getCache(String name) {
                return mock(Cache.class);
            }

            @Override
            public Collection<String> getCacheNames() {
                return null;
            }
        });

        TenantCacheManager cacheManager = new TenantCacheManager(cacheManagers);

        // act
        Cache actual;
        try (
            MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(() -> TenantContext.getCurrentTenant()).thenReturn("anyTenant");
            actual = cacheManager.getCache("anyTenant");
        }

        // assert
        assertThat(actual).isNotInstanceOf(NoOpCache.class);
    }

    @Test
    void getCacheNames_CacheNotExists_EmptyCollectionReturned() {
        TenantCacheManager cacheManager = new TenantCacheManager(new HashMap<>());

        // act
        Collection<String> actual;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(() -> TenantContext.getCurrentTenant()).thenReturn("anyTenant");
            actual = cacheManager.getCacheNames();
        }

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void getCacheNames_CacheExists_CollectionWithNamesReturned() {
        // arrange
        Map<String, CacheManager> cacheManagers = Map.of("anyTenant", new CacheManager() {
            @Override
            public Cache getCache(String name) {
                return mock(Cache.class);
            }

            @Override
            public Collection<String> getCacheNames() {
                return List.of("cache1", "cache2");
            }
        });

        TenantCacheManager cacheManager = new TenantCacheManager(cacheManagers);

        // act
        Collection<String> actual;
        try (
            MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(() -> TenantContext.getCurrentTenant()).thenReturn("anyTenant");
            actual = cacheManager.getCacheNames();
        }

        // assert
        assertThat(actual).hasSize(2);
    }
}
