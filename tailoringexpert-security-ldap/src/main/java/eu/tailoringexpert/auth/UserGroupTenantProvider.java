/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2025 Michael Bädorf and others
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
package eu.tailoringexpert.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Log4j2
@RequiredArgsConstructor
public class UserGroupTenantProvider implements Function<UserDetails, String> {
    @NonNull
    private Map<String, String> group2Tenant;

    public String apply(UserDetails user) {
        Optional<String> tenant = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(group2Tenant::containsKey)
            .findFirst()
            .map(group2Tenant::get);

        return tenant.orElseThrow(() -> log.throwing(
            new AuthenticationServiceException("User " + user.getUsername() + " does not belong to andy tenent"))
        );
    }
}
