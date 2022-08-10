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

import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;


/**
 * Container Objekt für die Speicherung einer Tenant Information im aktuellen
 * Thread.
 *
 * @author Michael Bädorf
 */
@SuppressWarnings({"java:S5164"})
@NoArgsConstructor(access = PRIVATE)
public class TenantContext {

    /**
     * Die im System registrierten Tenantschlüssel.
     */
    private static Set<String> registeredTenants = new HashSet<>();

    /**
     * Thread Local des zu verwendenden Tenants.
     */
    private static final ThreadLocal<String> tenantThreadLocal = new ThreadLocal<>();

    /**
     * Registriert einen Tenant(schlüssel).
     *
     * @param tenant Der zu registrierende Tenant
     */
    public static void registerTenant(final String tenant) {
        if (nonNull(tenant) && !tenant.trim().isBlank()) {
            registeredTenants.add(tenant);
        }
    }

    /**
     * Gibt die Liste aller registerierten Tenant(schlüssel) zurück.
     *
     * @return Alle registrierten Tenants
     */
    public static Set<String> getRegisteredTenants() {
        return registeredTenants;
    }

    /**
     * Setzt den zu verwendenden Tenants.
     *
     * @param tenant Der zu verwendende Tenant
     */
    public static void setCurrentTenant(final String tenant) {
        tenantThreadLocal.set(tenant);
    }

    /**
     * Gibt den zu verwendenden Tenant zurück.
     *
     * @return Der zu verwendende Tenant
     */
    public static String getCurrentTenant() {
        return tenantThreadLocal.get();
    }
}
