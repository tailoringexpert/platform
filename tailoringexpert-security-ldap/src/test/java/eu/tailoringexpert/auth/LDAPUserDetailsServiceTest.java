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

import eu.tailoringexpert.domain.Authentication;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.function.Function;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class LDAPUserDetailsServiceTest {

    AuthenticationManager authenticationManagerMock;
    LdapUserSearch userSearchMock;
    LdapAuthoritiesPopulator authoritiesPopulatorMock;
    Function<UserDetails, String> tenantProviderMock;
    JWTService jwtServiceMock;

    JWTService tokenProvider;
    LDAPUserDetailsService service;

    @BeforeEach
    void beforeEach() {
        this.tokenProvider = new JWTService("Test1234Test1234Test1234Test1234Test1234Test1234", 60000L, 600000L);

        this.authenticationManagerMock = mock(AuthenticationManager.class);
        this.userSearchMock = mock(LdapUserSearch.class);
        this.authoritiesPopulatorMock = mock(LdapAuthoritiesPopulator.class);
        this.tenantProviderMock = mock(Function.class);
        this.jwtServiceMock = mock(JWTService.class);

        this.service = spy(new LDAPUserDetailsService(
                authenticationManagerMock,
                userSearchMock,
                of("ROLE1"),
                authoritiesPopulatorMock,
            tenantProviderMock,
                jwtServiceMock
            )
        );
    }

    @Test
    void authenticate_UsernameNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Exception actual = catchException(() -> service.authenticate(null, "test1234!"));

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void authenticate_PasswordNull_NullPointerExceptionThrown() {
        // arrange

        // act
        Exception actual = catchException(() -> service.authenticate("f_demo", null));

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void authenticate_UserWithRequiredRoleDoesNotExit_NullReturned() {
        // arrange
        doThrow(new UsernameNotFoundException("User f_demo not found in directory."))
            .when(service).loadUserByUsername("f_demo");

        // act
        Throwable actual = catchThrowable(() -> service.authenticate("f_demo", "test1234!"));

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessage("User f_demo not found in directory.");

    }

    @Test
    void authenticate_UserWithRequiredRoleExits_AuthReturned() {
        // arrange
        String accessToken = tokenProvider.generateToken("f_demo", of("ROLE1"));
        String refreshToken = tokenProvider.generateRefreshToken("f_demo", of("ROLE1"));
        User user = new User("f_demo", "test1234!", of(new SimpleGrantedAuthority("ROLE1")));

        doReturn(user).when(service).loadUserByUsername("f_demo");
        given(tenantProviderMock.apply(user)).willReturn("demo");
        given(jwtServiceMock.generateToken("f_demo", of("ROLE1")))
            .willReturn(accessToken);
        given(jwtServiceMock.generateRefreshToken("f_demo", of("ROLE1")))
            .willReturn(refreshToken);

        // act
        Authentication actual = service.authenticate("f_demo", "test1234!");

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getUserId()).isEqualTo("f_demo");
        assertThat(actual.getTenant()).isEqualTo("demo");
        assertThat(actual.getAccessToken()).isEqualTo(accessToken);
        assertThat(actual.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void refresh_RefreshTokenExpired_ExceptionThrown() {
        // arrange
        String refreshToken = tokenProvider.generateRefreshToken("f_demo", of("ROLE_ROLE1", "ROLE_ROLE2"));
        Claims claims = Jwts.claims()
            .subject("f_demo")
            .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
            .build();

        given(jwtServiceMock.getClaimsOf(refreshToken))
            .willReturn(claims);
        given(jwtServiceMock.getUserNameOf(claims))
            .willReturn("f_demo");
        given(jwtServiceMock.isTokenExpired(claims))
            .willReturn(true);


        // act
        Throwable actual = catchThrowable(() -> service.refresh("f_demo", refreshToken));

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(AuthenticationServiceException.class)
            .hasMessage("Refrestoken expired");
    }

    @Test
    void refresh_UserNotOwnerOfToken_ExceptionThrown() {
        // arrange
        String refreshToken = tokenProvider.generateRefreshToken("f_demo", of("ROLE_ROLE1", "ROLE_ROLE2"));
        Claims claims = Jwts.claims()
            .subject("f_demo")
            .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
            .build();

        given(jwtServiceMock.getClaimsOf(refreshToken))
            .willReturn(claims);
        given(jwtServiceMock.getUserNameOf(claims))
            .willReturn("f_demo");

        given(jwtServiceMock.isTokenExpired(claims))
            .willReturn(true);

        // act
        Throwable actual = catchThrowable(() -> service.refresh("f_dummy", refreshToken));

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(AuthenticationServiceException.class)
            .hasMessage("User not owner of token");
    }

    @Test
    void refresh_RefreshTokenValid_NewTokenGenerated() {
        // arrange
        String refreshToken = tokenProvider.generateRefreshToken("f_demo", of("ROLE_ROLE1", "ROLE_ROLE2"));
        Claims claims = Jwts.claims()
            .subject("f_demo")
            .add("GRANTED_AUTHORITIES", of("ROLE_ROLE1", "ROLE_ROLE2"))
            .build();

        given(jwtServiceMock.getClaimsOf(refreshToken))
            .willReturn(claims);
        given(jwtServiceMock.getUserNameOf(claims))
            .willReturn("f_demo");
        given(jwtServiceMock.isTokenExpired(claims))
            .willReturn(false);
        given(jwtServiceMock.extractGrantedAuthorities(claims))
            .willReturn(of("ROLE_ROLE1", "ROLE_ROLE2"));
        given(jwtServiceMock.generateToken("f_demo", of("ROLE_ROLE1", "ROLE_ROLE2")))
            .willReturn(tokenProvider.generateToken("f_demo", of("ROLE_ROLE1", "ROLE_ROLE2")));
        given(jwtServiceMock.generateRefreshToken("f_demo", of("ROLE_ROLE1", "ROLE_ROLE2")))
            .willReturn(tokenProvider.generateRefreshToken("f_demo", of("ROLE_ROLE1", "ROLE_ROLE2")));

        // act
        Authentication actual = service.refresh("f_demo", refreshToken);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getAccessToken())
            .isNotNull()
            .isNotEqualTo(refreshToken);
    }

    @Test
    void authenticate_UserWithNoTenant_ExceptionThrown() {
        // arrange
        User user = new User("f_demo", "test1234!", of(new SimpleGrantedAuthority("ROLE1")));

        doReturn(user).when(service).loadUserByUsername("f_demo");
        doThrow(new AuthenticationServiceException("User f_demo does not belong to any tenant"))
            .when(tenantProviderMock).apply(user);

        // act
        Throwable actual = catchThrowable(() -> service.authenticate("f_demo", "test1234!"));

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(AuthenticationServiceException.class)
            .hasMessage("User f_demo does not belong to any tenant");

        verify(jwtServiceMock, times(0)).generateToken(any(), any());
        verify(jwtServiceMock, times(0)).generateRefreshToken(any(), any());
    }
}
