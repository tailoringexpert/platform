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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class JWTRequestFilterTest {

    JWTService jwtServiceMock;
    FilterChain filterChainMock;
    JWTService tokenProvider;

    JWTRequestFilter filter;

    @BeforeEach
    void setUp() {
        this.tokenProvider = new JWTService("Test1234Test1234Test1234Test1234Test1234Test1234", 6000L, 60000L);
        this.jwtServiceMock = mock(JWTService.class);
        this.filterChainMock = mock(FilterChain.class);
        this.filter = spy(new JWTRequestFilter(jwtServiceMock));

        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @SneakyThrows
    @Test
    void doFilterInternal_AuthHeaderNull_RequestNotVerifiedChainExecuted() {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);

        given(requestMock.getHeader("Authorization"))
                .willReturn(null);
        // act
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        // assert
        verify(filter, times(0)).verifyAndAuthenticate(any(), any());
        verify(filterChainMock, times(1)).doFilter(requestMock, responseMock);
    }

    @SneakyThrows
    @Test
    void doFilterInternal_AuthHeaderNotStartWithBearer_RequestNotVerifiedChainExecuted() {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);

        given(requestMock.getHeader("Authorization"))
                .willReturn("Bierer");
        // act
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        // assert
        verify(filter, times(0)).verifyAndAuthenticate(any(), any());
        verify(filterChainMock, times(1)).doFilter(requestMock, responseMock);
    }

    @SneakyThrows
    @Test
    void doFilterInternal_VaildAuthHeader_AuthVerified() {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        String authHeader = "Bearer " + tokenProvider.generateToken("f_demo", of());

        given(requestMock.getHeader("Authorization"))
                .willReturn(authHeader);
        // act
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        // assert
        verify(filter, times(1)).verifyAndAuthenticate(requestMock, authHeader);
        verify(filterChainMock, times(1)).doFilter(requestMock, responseMock);
    }

    @SneakyThrows
    @Test
    void doFilterInternal_VerifyThrowsException_ChainExecuted() {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        String authHeader = "Bearer " + tokenProvider.generateToken("f_demo", of());

        given(requestMock.getHeader("Authorization"))
                .willReturn(authHeader);
        doThrow(new RuntimeException("verifyAndAuthenticate"))
                .when(filter).verifyAndAuthenticate(requestMock, authHeader);
        // act
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        // assert
        verify(filter, times(1)).verifyAndAuthenticate(requestMock, authHeader);
        verify(filterChainMock, times(1)).doFilter(requestMock, responseMock);
    }


    @Test
    void testDoFilterInternalWhenSuccessAuthenticationShouldNotAppendAnyLog() throws Exception {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        String token = "Bearer " + tokenProvider.generateToken("f_tailor", of());

        given(requestMock.getHeader("Authorization"))
                .willReturn(token);

        // act
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        // assert
        verify(filter, times(1)).verifyAndAuthenticate(any(), any());
        verify(filterChainMock).doFilter(requestMock, responseMock);
    }

    @Test
    void testDoFilterInternalWhenNoTokenFoundShouldNotCallVerifyMethod() throws Exception {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);

        given(requestMock.getHeader("Authorization"))
                .willReturn(null);

        // act
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        // assert
        verify(filter, times(0)).verifyAndAuthenticate(eq(requestMock), anyString());
        verify(filterChainMock).doFilter(requestMock, responseMock);
    }


    @Test
    void testDoFilterInternalWhenExceptionOccuredShouldAppendErrorLog() throws Exception {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        HttpServletResponse responseMock = mock(HttpServletResponse.class);
        String token = "Bearer " + tokenProvider.generateToken("f_tailor", of());

        given(requestMock.getHeader("Authorization")).
                willReturn(token);

        doThrow(new UsernameNotFoundException("Authentication failed."))
                .when(filter).verifyAndAuthenticate(requestMock, token);

        // act
        filter.doFilterInternal(requestMock, responseMock, filterChainMock);

        // assert
        verify(filter, times(1)).verifyAndAuthenticate(requestMock, token);
        verify(filterChainMock).doFilter(requestMock, responseMock);
    }


    @Test
    void verifyAndAuthenticate_TokenNotExpired_SecurityContextWithoutAuth() {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String token = "Bearer " + tokenProvider.generateToken("f_tailor", of());
        Claims claims = Jwts.claims()
                .subject("f_tailor")
                .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
                .build();

        given(requestMock.getHeader("Authorization")).
                willReturn(token);
        given(jwtServiceMock.isTokenExpired(claims))
                .willReturn(true);

        // act
        filter.verifyAndAuthenticate(requestMock, token);

        // assert

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }


    @Test
    void testVerifyAndAuthenticateUserWhenTokenVerifiedShouldHaveSecurityContextWithAuth() {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String token = tokenProvider.generateToken("f_tailor", of());
        String bearer = "Bearer " + token;

        Claims claims = Jwts.claims()
                .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
                .subject("f_tailor")
                .build();

        SecurityContextHolder.getContext().setAuthentication(null);

        given(jwtServiceMock.getClaimsOf(token))
                .willReturn(claims);
        given(jwtServiceMock.getUserNameOf(claims))
                .willReturn("f_tailor");
        given(jwtServiceMock.extractGrantedAuthorities(claims))
                .willReturn(of("ROLE_ROLE1", "ROLE_ROLE2"));
        given(jwtServiceMock.isTokenExpired(claims))
                .willReturn(false);

        // act
        filter.verifyAndAuthenticate(requestMock, bearer);

        // assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("f_tailor");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEqualTo(
                of(new SimpleGrantedAuthority("ROLE_ROLE1"), new SimpleGrantedAuthority("ROLE_ROLE2")));
        assertThat(SecurityContextHolder.getContext().getAuthentication().getCredentials()).isNull();
    }

    @Test
    void verifyAndAuthenticate_UserAndAthContextAvailabe() {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String token = tokenProvider.generateToken("f_tailor", of());
        String bearer = "Bearer " + token;
        List<GrantedAuthority> userRoles = of(new SimpleGrantedAuthority("FINANCE"));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("f_tailor", "test1234", userRoles);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        given(jwtServiceMock.getUserNameOf(any()))
                .willAnswer((Answer<String>) invocation -> "f_tailor");
        given(jwtServiceMock.isTokenExpired(any()))
            .willReturn(false);

        // act
        filter.verifyAndAuthenticate(requestMock, bearer);

        // assert
        verify(jwtServiceMock, times(0)).isTokenExpired(any(Claims.class));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(authentication);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .isEqualTo(of(new SimpleGrantedAuthority("FINANCE")));
    }

    @Test
    void verifyAndAuthenticate_TokenExpired_AuthenticationServiceExceptionThrown() {
        // arrange
        HttpServletRequest requestMock = mock(HttpServletRequest.class);
        String token = tokenProvider.generateToken("f_demo", of());
        String bearer = "Bearer " + token;
        Claims claims = Jwts.claims()
                .subject("f_demo")
                .add("grantedAuthorities", of("ROLE_ROLE1", "ROLE_ROLE2"))
                .build();

        given(jwtServiceMock.getClaimsOf(token))
                .willReturn(claims);
        given(jwtServiceMock.getUserNameOf(claims))
                .willReturn("f_demo");
        given(jwtServiceMock.isTokenExpired(claims))
                .willReturn(true);

        // act
        Throwable actual = catchThrowable(() -> filter.verifyAndAuthenticate(requestMock, bearer));

        // assert
        assertThat(actual).isNotNull()
                .isInstanceOf(AuthenticationServiceException.class);
        verify(jwtServiceMock, times(1)).isTokenExpired(claims);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
