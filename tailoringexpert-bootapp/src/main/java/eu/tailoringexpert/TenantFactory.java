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

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.Files.newInputStream;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@Log4j2
@AllArgsConstructor
public class TenantFactory {

    @NonNull
    private DataSource defaultDataSource;

    @NonNull
    private String tenantConfigRoot;

    @NonNull
    private StringEncryptor encryptor;


    public Map<String, String> tenants() {
        log.debug("Search tenant configuration in " + Paths.get(tenantConfigRoot).toFile());

        try (Stream<Path> files = findByFileExtension(Paths.get(tenantConfigRoot), ".properties")) {
            files.map(Path::toFile)
                .forEach(propertyFile -> {
                    final Properties tenantProperties = loadProperties(propertyFile);
                    final String tenantId = tenantProperties.getProperty("id");
                    final String tenantName = tenantProperties.getProperty("name");
                    TenantContext.registerTenant(tenantId, tenantName);
                });
        }
        return TenantContext.getRegisteredTenants();
    }

    /**
     * Creates and register tenants and corresponding datasources.
     *
     * @return TenantDataSource
     */
    public DataSource dataSource() {
        log.debug("Search tenant db configuration in " + Paths.get(tenantConfigRoot).toFile());

        final Map<Object, Object> resolvedDataSources = new HashMap<>();
        try (Stream<Path> files = findByFileExtension(Paths.get(tenantConfigRoot), ".properties")) {
            files.map(Path::toFile)
                .forEach(propertyFile -> {
                    log.debug(propertyFile.getAbsolutePath());
                    final Properties tenantProperties = loadProperties(propertyFile);
                    final String tenantId = tenantProperties.getProperty("id");
                    final DataSource tenantDataSource = buildDataSource(tenantProperties);
                    resolvedDataSources.put(tenantId, tenantDataSource);
                });
        }
        if (resolvedDataSources.isEmpty()) {
            log.error("No tenant datasources are available!!!");
        }
        // Create the final multi-tenant source.
        // It needs a default database to connect to.
        // Make sure that the default database is actually an empty tenant database.
        // Don't use that for a regular tenant if you want things to be safe!
        final TenantDataSource result = new TenantDataSource();
        result.setDefaultTargetDataSource(defaultDataSource);

        result.setTargetDataSources(resolvedDataSources);

        // Call this to finalize the initialization of the data source.
        result.afterPropertiesSet();

        return result;
    }

    DataSource buildDataSource(final Properties properties) {
        final DriverManagerDataSource result = new DriverManagerDataSource();
        result.setDriverClassName(properties.getProperty("spring.datasource.driver-class-name"));
        result.setUrl(properties.getProperty("spring.datasource.url"));
        result.setUsername(properties.getProperty("spring.datasource.username"));
        result.setPassword(properties.getProperty("spring.datasource.password"));
        return result;
    }


    /**
     * Load propertyfile and replaces placeholder.
     *
     * @param file property file to load
     * @return loaded encrypted properties
     */
    @SneakyThrows
    Properties loadProperties(final File file) {
        final Properties properties = new Properties();
        try (InputStream fis = newInputStream(file.toPath())) {
            properties.load(fis);
        }

        properties.forEach((key, value) -> properties.replace(key, resolveEnvVar((String) value)));

        return new EncryptableProperties(properties, encryptor);
    }

    @SneakyThrows
    Stream<Path> findByFileExtension(Path path, String fileExtension) {
        return Files.walk(path, 1)
            .filter(p -> p.getFileName().toString().endsWith(fileExtension));
    }

    String resolveEnvVar(String key) {
        Pattern p = Pattern.compile("\\$\\{(\\w+)}|\\$(\\w+)");
        Matcher m = p.matcher(key);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String name = null == m.group(1) ? m.group(2) : m.group(1);
            String value = ofNullable(System.getProperty(name)).orElse(System.getenv(name));
            m.appendReplacement(sb, isNull(value) ? "" : Matcher.quoteReplacement(value));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
