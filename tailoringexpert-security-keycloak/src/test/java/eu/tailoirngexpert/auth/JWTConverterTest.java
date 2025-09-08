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
package eu.tailoirngexpert.auth;

import eu.tailoringexpert.auth.JWTConverter;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.List;

import static java.util.Map.entry;
import static java.util.Map.of;
import static java.util.Map.ofEntries;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

@Log4j2
class JWTConverterTest {

    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;
    JWTConverter converter;

    @BeforeEach
    void beforeEach() {
        this.jwtGrantedAuthoritiesConverterMock = Mockito.<JwtGrantedAuthoritiesConverter>mock(JwtGrantedAuthoritiesConverter.class);
        this.converter = new JWTConverter(
            "tailoringexpert",
            "tailoringexpert",
            this.jwtGrantedAuthoritiesConverterMock
        );
    }

    @Test
    void convert_NullJWT_NullPointerExceptionThrown() {
        // arrange

        // act
        Throwable actual = catchThrowable(() -> converter.convert(null));

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void convert_ResourceAccessClaimNotExists_AuthenticatedTokenReturned() {
        // arrange
        String token = "Not relevant for conversion test";

        Jwt jwt = Jwt.withTokenValue(token)
            .header("kid", "test")
            .header("typ", "JWT")
            .header("alg", "RS256")
            .claim("typ", "Bearer")
            .claim("scope", "openid mail profile")
            .build();

        given(jwtGrantedAuthoritiesConverterMock.convert(jwt)).willReturn(List.of(
            new SimpleGrantedAuthority("SCOPE_openid"),
            new SimpleGrantedAuthority("SCOPE_email"),
            new SimpleGrantedAuthority("SCOPE_profile")
        ));

        // act
        AbstractAuthenticationToken actual = converter.convert(jwt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.isAuthenticated()).isTrue();
        assertThat(actual.getAuthorities()).hasSize(3);
        assertThat(actual.getAuthorities()).containsOnly(
            new SimpleGrantedAuthority("SCOPE_openid"),
            new SimpleGrantedAuthority("SCOPE_email"),
            new SimpleGrantedAuthority("SCOPE_profile")
        );
    }

    @Test
    void convert_ResourceAccessClaimExistsResourceIdNotExists_AuthenticatedTokenReturned() {
        // arrange
        String token = "Not relevant for conversion test";

        Jwt jwt = Jwt.withTokenValue(token)
            .header("kid", "test")
            .header("typ", "JWT")
            .header("alg", "RS256")
            .claim("typ", "Bearer")
            .claim("scope", "openid mail profile")
            .claim("resource_access", of("account", of("roles", List.of("manage-account", "manage-account-links", "view-profile"))))
            .build();

        given(jwtGrantedAuthoritiesConverterMock.convert(jwt)).willReturn(List.of(
            new SimpleGrantedAuthority("SCOPE_openid"),
            new SimpleGrantedAuthority("SCOPE_email"),
            new SimpleGrantedAuthority("SCOPE_profile")
        ));
        // act
        AbstractAuthenticationToken actual = converter.convert(jwt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.isAuthenticated()).isTrue();
        assertThat(actual.getAuthorities()).hasSize(3);
        assertThat(actual.getAuthorities()).containsOnly(
            new SimpleGrantedAuthority("SCOPE_openid"),
            new SimpleGrantedAuthority("SCOPE_email"),
            new SimpleGrantedAuthority("SCOPE_profile")
        );
    }


    @Test
    void convert_ResourceAccessClaimExistsResourceIdExists_AuthenticatedTokenWithConvertedRolesAndScopesReturned() {
        // arrange
        String token = "Not relevant for conversion test";

        Jwt jwt = Jwt.withTokenValue(token)
            .header("kid", "test")
            .header("typ", "JWT")
            .header("alg", "RS256")
            .claim("typ", "Bearer")
            .claim("scope", "openid mail profile")
            .claim("resource_access", ofEntries(
                    entry("tailoringexpert", of("roles", List.of("manage-account", "manage-account-links", "view-profile")))
                )
            )
            .build();

        given(jwtGrantedAuthoritiesConverterMock.convert(jwt)).willReturn(List.of(
            new SimpleGrantedAuthority("SCOPE_openid"),
            new SimpleGrantedAuthority("SCOPE_email"),
            new SimpleGrantedAuthority("SCOPE_profile")
        ));
        // act
        AbstractAuthenticationToken actual = converter.convert(jwt);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.isAuthenticated()).isTrue();
        assertThat(actual.getAuthorities()).hasSize(6);
        assertThat(actual.getAuthorities()).contains(
            new SimpleGrantedAuthority("ROLE_manage-account"),
            new SimpleGrantedAuthority("ROLE_view-profile"),
            new SimpleGrantedAuthority("ROLE_manage-account-links")
        );
    }
}
