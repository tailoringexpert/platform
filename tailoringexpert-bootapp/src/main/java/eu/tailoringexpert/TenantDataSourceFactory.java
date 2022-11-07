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

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.Files.newInputStream;
import static lombok.AccessLevel.PRIVATE;

@Log4j2
@NoArgsConstructor(access = PRIVATE)
public class TenantDataSourceFactory {

    /**
     * Erzeugt die Datasources der Teants.
     *
     * @param defaultDataSource
     * @param tenantConfigDir
     * @return
     * @throws IOException
     */
    public static DataSource dataSource(final DataSource defaultDataSource, final String tenantConfigDir, StringEncryptor encryptor)
        throws IOException {
        log.info("Suche Tenant DB-Konfigurationen in " + Paths.get(tenantConfigDir).toFile());

        final Map<Object, Object> resolvedDataSources = new HashMap<>();
        try (Stream<Path> files = Files.walk(Paths.get(tenantConfigDir))) {
            files.map(Path::toFile)
                .filter(file -> "db.properties".equalsIgnoreCase(file.getName()))
                .forEach(propertyFile -> {
                    final Properties tenantProperties = loadProperties(propertyFile, encryptor);
                    final String tenantId = tenantProperties.getProperty("name");
                    final DataSource tenantDataSource = buildDataSource(tenantProperties);
                    resolvedDataSources.put(tenantId, tenantDataSource);
                    TenantContext.registerTenant(tenantId);

                });
        }
        // Create the final multi-tenant source.
        // It needs a default database to connect to.
        // Make sure that the default database is actually an empty tenant
        // database.
        // Don't use that for a regular tenant if you want things to be safe!
        final TenantDataSource result = new TenantDataSource();
        result.setDefaultTargetDataSource(defaultDataSource);

        result.setTargetDataSources(resolvedDataSources);

        // Call this to finalize the initialization of the data source.
        result.afterPropertiesSet();

        return result;
    }

    private static DataSource buildDataSource(final Properties properties) {
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
     * @return properties with replaced placeholders
     * @throws IOException Fehler beim einlesen der File
     */
    @SneakyThrows
    private static Properties loadProperties(final File file, final StringEncryptor encryptor) {
        final Properties properties = new Properties();
        try (InputStream fis = newInputStream(file.toPath())) {
            properties.load(fis);
        }

        properties.entrySet().forEach(entry -> entry.setValue(resolvePlaceholder(entry.getValue().toString())));
        return new EncryptableProperties(properties, encryptor);
    }

    /**
     * Ersetzt den Wert von System- und Umgebungsvariablen.
     *
     * @param input Der zu analysierende String
     * @return Neuer geparster String aus dem Eingabewert
     */
    private static String resolvePlaceholder(final String input) {
        if (input == null) {
            return null;
        }

        final Pattern p = Pattern.compile("\\$\\{([^}]*)\\}");
        final Matcher m = p.matcher(input); // get a matcher object
        final StringBuffer sb = new StringBuffer();
        while (m.find()) {
            final String name = null == m.group(1) ? m.group(2) : m.group(1);
            String value = System.getProperty(name);
            if (value == null) {
                value = System.getenv(name);
            }

            value = value.replace("\\", "\\\\");
            m.appendReplacement(sb, Objects.nonNull(value) ? value : "");
        }
        m.appendTail(sb);

        return sb.toString().trim();
    }
}
