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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class TenantCacheManager implements CacheManager {

    @NonNull
    private Map<String, CacheManager> cacheManagers;

    @Override
    @Nullable
    public Cache getCache(String name) {
        CacheManager cacheManager = cacheManagers.get(TenantContext.getCurrentTenant());
        return cacheManager.getCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        CacheManager cacheManager = cacheManagers.get(TenantContext.getCurrentTenant());
        Set<String> names = new LinkedHashSet<>(cacheManager.getCacheNames());
        return Collections.unmodifiableSet(names);
    }
}
