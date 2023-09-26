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

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static java.util.Collections.singletonList;
import static java.util.List.of;
import static org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED;

/**
 * Security Configuration to be used on a local system.
 *
 * @author Michael Bädorf
 */
@Configuration
@EnableWebSecurity
@Log4j2
public class SecurityConfiguration {

    public SecurityConfiguration() {
        log.info("Using SecurityConfiguration");
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll());
        http.cors(Customizer.withDefaults());
        http.headers(headers -> headers
            .xssProtection(xss -> xss.headerValue(ENABLED))
            .contentSecurityPolicy(policy -> policy.policyDirectives("script-src 'self'"))
        );
        return http.build();
    }


}
