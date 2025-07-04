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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static java.util.List.of;
import static java.util.Objects.nonNull;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Log4j2
@Configuration
@EnableWebSecurity
public class LDAPSecurityConfiguration {

    @Bean
    LdapUserSearch ldapUserSearch(
        @Value("${ldap.user.base}") String userSearchBase,
        @Value("${ldap.user.filter}") String userSearchFilter,
        @NonNull BaseLdapPathContextSource contextSource
    ) {
        return new FilterBasedLdapUserSearch(userSearchBase, userSearchFilter, contextSource);
    }

    @Bean
    LdapAuthoritiesPopulator ldapAuthoritiesPopulator(
        @Value("${ldap.group.base}") String groupBase,
        @NonNull ContextSource contextSource) {
        return new DefaultLdapAuthoritiesPopulator(contextSource, groupBase);
    }

    @Bean
    AuthenticationManager authenticationManager(
        @Value("${ldap.user.base}") String userSearchBase,
        @Value("${ldap.user.filter}") String userSearchFilter,
        BaseLdapPathContextSource contextSource) {
        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserSearchFilter(userSearchFilter);
        factory.setUserSearchBase(userSearchBase);
        return factory.createAuthenticationManager();
    }

    @Bean
    org.springframework.security.core.userdetails.UserDetailsService userDetailsService(
        @Value("${ldap.group.defined}") Collection<String> definedRoles,
        @NonNull AuthenticationManager authenticationManager,
        @NonNull LdapUserSearch userSearch,
        @NonNull LdapAuthoritiesPopulator authoritiesPopulator,
        @NonNull JWTService jwtService

    ) {
        return new LDAPUserDetailsService(
            authenticationManager,
            userSearch,
            definedRoles,
            authoritiesPopulator,
            jwtService
        );
    }

    @Bean
    JWTService jwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expires.token}") long expiresToken,
        @Value("${jwt.expires.refresh}") long expiresRefresh
    ) {
        return new JWTService(secret, expiresToken, expiresRefresh);
    }

    @Bean
    JWTRequestFilter jwtRequestFilter(
        @NonNull JWTService jwtService
    ) {
        return new JWTRequestFilter(jwtService);
    }

    @Bean
    AuthenticationController authenticationController(
        @NonNull @Value("${server.servlet.context-path}") String contextPath,
        @NonNull LDAPUserDetailsService userDetailsService
    ) {
        return new AuthenticationController(
            contextPath,
            userDetailsService
        );
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(of("*"));
        configuration.setAllowedOrigins(singletonList("*"));
        configuration.setAllowedMethods(of("*"));
        configuration.setExposedHeaders(of("*"));
        UrlBasedCorsConfigurationSource result = new UrlBasedCorsConfigurationSource();
        result.registerCorsConfiguration("/**", configuration);
        return result;
    }

    @Bean
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    SecurityFilterChain securityFilterChain(
        @NonNull HttpSecurity http,
        @NonNull JWTRequestFilter jwtRequestFilter,
        @Value("${auth.permit-all}") String[] allPermissions,
        @Value("${auth.authenticated}") String[] authenticatedPath,
        @Qualifier("rolePermissions") Map<String, String[]> rolePermissions
    ) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .headers(headers ->
                headers
                    .contentSecurityPolicy(policy -> policy.policyDirectives("script-src 'self'"))
            )
            .authorizeHttpRequests(auth -> {
                    rolePermissions.forEach((role, paths) ->
                        auth.requestMatchers(paths).hasRole(role)
                    );
                    auth.requestMatchers(allPermissions).permitAll();
                    if (nonNull(authenticatedPath) && authenticatedPath.length > 0) {
                        auth.requestMatchers(authenticatedPath).authenticated();
                    }
                }
            )
            .sessionManagement((Customizer.withDefaults()))
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(sessionManagementCustomizer ->
                sessionManagementCustomizer.sessionCreationPolicy(STATELESS)
            );


        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
