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

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import eu.tailoringexpert.domain.ResourceMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.json.JsonMapper.Builder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Locale.GERMANY;
import static tools.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT;

@Log4j2
@Configuration
public class RestConfiguration {

    @Bean
    ObjectMapper objectMapper(@Value("#{${mixIns}}") List<String> mixIns) {
        Builder result = JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd", GERMANY))
            .findAndAddModules()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
            .handlerInstantiator(new HalJacksonModule.HalHandlerInstantiator(new EvoInflectorLinkRelationProvider(),
                CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY))
            .withConfigOverride(List.class, cfg ->
                cfg.setNullHandling(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY))
            );

        Optional.ofNullable(mixIns)
            .ifPresent(oMixIns ->
                oMixIns.forEach(mixIn -> {
                    String[] config = mixIn.split(":");
                    try {
                        log.info("Register MixIn {} for {}", config[1], config[0]);
                        result.addMixIn(Class.forName(config[0]), Class.forName(config[1]));
                    } catch (ClassNotFoundException e) {
                        throw log.throwing(new RuntimeException(e));
                    }
                }));

        return result.build();
    }

    @Bean
    HalConfiguration halConfiguration() {
        return new HalConfiguration().withRenderSingleLinksFor("phasen", HalConfiguration.RenderSingleLinks.AS_ARRAY);
    }

    @Bean
    public ByteArrayHttpMessageConverter byteArrayHttpMessageConverter() {
        final ByteArrayHttpMessageConverter result = new ByteArrayHttpMessageConverter();
        result.setSupportedMediaTypes(asList(
            MediaType.IMAGE_JPEG,
            MediaType.IMAGE_PNG,
            MediaType.APPLICATION_OCTET_STREAM,
            new MediaType("application", "vnd.openxmlformats-officedocument.wordprocessingml.document")
        ));
        return result;
    }

    @Bean
    AppController appController(@NonNull ResourceMapper resourceMapper) {
        return new AppController(resourceMapper);
    }


    @Bean("tenantRequestFilter")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    OncePerRequestFilter tenantRequestFilter() {
        return new OncePerRequestFilter() {

            /**
             * {@inheritDoc}
             */
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
                final String tenant = request.getHeader("x-tenant");
                TenantContext.setCurrentTenant(tenant);
                chain.doFilter(request, response);
            }
        };
    }

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.version}") String version) {
        return new OpenAPI().info(
            new Info()
                .title("Tailoring API")
                .version(version)
                .description("Tailoringexpert is a multi tenant platform to create easily, fast and reproduceable requirement documentation based on a general requirement catalog on a limited set of parameters, which characterize the specific project.")
                .license(new License()
                    .name("GNU General Public License v3.0")
                    .url("https://www.gnu.org/licenses/gpl-3.0")
                )
        );
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) ->
            operation.addParametersItem(
                new Parameter()
                    .in("header")
                    .required(true)
                    .description("Tenant using API")
                    .name("X-Tenant")
                    .schema(new StringSchema())
            );
    }
}
