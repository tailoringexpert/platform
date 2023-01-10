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
package eu.tailoringexpert.renderer;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.thymeleaf.templatemode.TemplateMode.HTML;

@Configuration
public class RendererConfiguration {

    @Bean
    SpringTemplateEngine springTemplateEngine(@NonNull @Value("${templateRoot}") final String templateRoot) {
        SpringTemplateEngine result = new SpringTemplateEngine();
        FileTemplateResolver fileTemplateResolver = new FileTemplateResolver();
        fileTemplateResolver.setPrefix(templateRoot);
        fileTemplateResolver.setCacheable(false);
        fileTemplateResolver.setSuffix(".html");
        fileTemplateResolver.setTemplateMode(HTML);
        fileTemplateResolver.setCharacterEncoding(UTF_8.toString());
        fileTemplateResolver.setOrder(1);
        fileTemplateResolver.setCheckExistence(true);
        fileTemplateResolver.setName("base");
        result.addTemplateResolver(fileTemplateResolver);

        return result;
    }

    @Bean
    RendererRequestConfigurationSupplier rendererRequestConfigurationSupplier(
        @NonNull @Value("${templateRoot}") final String templateRoot) {
        return new TenantRendererConfigurationSupplier(templateRoot);
    }

    @Bean
    HTMLTemplateEngine templateEngine(
        @NonNull SpringTemplateEngine templateEngine,
        @NonNull RendererRequestConfigurationSupplier rendererRequestConfigurationSupplier) {
        return new TenantTemplateEngine(
            htmlTemplateEngine(templateEngine, rendererRequestConfigurationSupplier),
            rendererRequestConfigurationSupplier);
    }

    private HTMLTemplateEngine htmlTemplateEngine(
        @NonNull SpringTemplateEngine templateEngine,
        @NonNull RendererRequestConfigurationSupplier rendererRequestConfigurationSupplier) {
        return new ThymeleafTemplateEngine(templateEngine, rendererRequestConfigurationSupplier);
    }

    @Bean
    PDFEngine pdfEngine(@NonNull RendererRequestConfigurationSupplier rendererRequestConfigurationSupplier) {
        return new PDFEngine(rendererRequestConfigurationSupplier);
    }
}
