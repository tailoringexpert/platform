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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class UserGroupTenantProviderTest {

    UserGroupTenantProvider mapper;

    @BeforeEach
    public void beforeEach() {
        Map<String, String> group2Tenant = Map.of("group_demo", "demo");
        this.mapper = new UserGroupTenantProvider(group2Tenant);
    }

    @Test
    void apply_groupWithNotExistingTenant_exceptionThrown() {
        // arrange
        UserDetails user = User.builder()
            .username("test")
            .password("none")
            .authorities(of(
                new SimpleGrantedAuthority("group_1"),
                new SimpleGrantedAuthority("group_2"),
                new SimpleGrantedAuthority("group_4")
            ))
            .build();

        // act
        Throwable actual = catchThrowable(() -> mapper.apply(user));

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(AuthenticationException.class)
            .hasMessage("User test does not belong to andy tenent");
    }

    @Test
    void apply_groupWithExistingTenant_tenantReturned() {
        // arrange
        UserDetails user = User.builder()
            .username("test")
            .password("none")
            .authorities(of(
                new SimpleGrantedAuthority("group_1"),
                new SimpleGrantedAuthority("group_2"),
                new SimpleGrantedAuthority("group_4"),
                new SimpleGrantedAuthority("group_demo")
            ))
            .build();

        // act
        String actual = mapper.apply(user);

        // assert
        assertThat(actual)
            .isNotNull()
            .isEqualTo("demo");
    }
}
