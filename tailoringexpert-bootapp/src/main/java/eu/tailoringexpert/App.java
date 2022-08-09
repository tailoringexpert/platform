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
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import static org.springframework.context.annotation.FilterType.REGEX;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        ThymeleafAutoConfiguration.class
    }
)
@ComponentScan(
    basePackages = {"eu.tailoringexpert"},
    excludeFilters = {
        @Filter(type = REGEX, pattern = ".*MapperImpl_?"),
        @Filter(RestController.class),
    }
)
@Log4j2
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);

    }

    @Bean
    PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        final PropertySourcesPlaceholderConfigurer result = new PropertySourcesPlaceholderConfigurer();
        result.setOrder(0);
        result.setIgnoreUnresolvablePlaceholders(false);
        result.setNullValue("@null");
        return result;
    }


    @EventListener
    public static void handleContextRefresh(ContextRefreshedEvent event) {
        final Environment env = event.getApplicationContext().getEnvironment();
        log.info("====== Environment and configuration ======");
        log.info("Active profiles: {}", Arrays.toString(env.getActiveProfiles()));
        final MutablePropertySources sources = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(sources.spliterator(), false)
            .filter(EnumerablePropertySource.class::isInstance)
            .map(EnumerablePropertySource.class::cast)
            .map(EnumerablePropertySource::getPropertyNames)
            .flatMap(Arrays::stream)
            .distinct()
            .filter(prop -> !(prop.contains("credentials") || prop.contains("password")))
            .forEach(prop -> log.info("{}: {}", prop, env.getProperty(prop)));
        log.info("===========================================");
    }


}
