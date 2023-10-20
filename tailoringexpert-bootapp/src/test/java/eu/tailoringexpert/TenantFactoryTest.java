/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2023 Michael Bädorf and others
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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class TenantFactoryTest {

    StandardPBEStringEncryptor encryptor;

    @BeforeEach
    void beforeEach() {
        this.encryptor = new StandardPBEStringEncryptor();
        this.encryptor.setPassword("TailoringForDemo!");
        this.encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
    }

    @AfterEach
    void afterEach() throws Exception {
        Field field = TenantContext.class.getDeclaredField("registeredTenants");
        field.setAccessible(true);
        field.set(null, new HashMap<>());
        field.setAccessible(false);
    }

    @Test
    void tenants_ValidConfigRootNoPropertyFiles_EmptyMapReturned() {
        // arrange
        String tenantConfigRoot = Paths.get("tenants").toAbsolutePath().toString();

        // act
        Map<String, String> actual = null;
        try (MockedStatic<TenantFactory> tf = mockStatic(TenantFactory.class)) {
            tf.when(() -> TenantFactory.findByFileExtension(eq(Paths.get(tenantConfigRoot)), eq(".properties")))
                .thenReturn(Stream.<Path>empty());

            tf.when(() -> TenantFactory.tenants(any(), any())).thenCallRealMethod();
            actual = TenantFactory.tenants(tenantConfigRoot, this.encryptor);
        }

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void tenants_ValidConfigRootPropertyFileExists_MapWithTenantReturned() throws Exception {
        // arrange
        String tenantConfigRoot = Paths.get("tenants").toAbsolutePath().toString();
        File file = createFile();
        Properties properties = createProperties();

        // act
        Map<String, String> actual = null;
        try (MockedStatic<TenantFactory> tf = mockStatic(TenantFactory.class)) {
            tf.when(() -> TenantFactory.findByFileExtension(eq(Paths.get(tenantConfigRoot)), eq(".properties")))
                .thenReturn(Stream.of(file.toPath()));
            tf.when(() -> TenantFactory.loadProperties(eq(file), eq(this.encryptor)))
                .thenReturn(properties);

            tf.when(() -> TenantFactory.tenants(any(), any())).thenCallRealMethod();
            actual = TenantFactory.tenants(tenantConfigRoot, this.encryptor);
        }

        // assert
        assertThat(actual).hasSize(1);
        assertThat(actual.get("demo")).isNotEmpty();
    }

    @Test
    void datasource_ValidConfigRootPropertyFileExists_TenantDataSourceReturned() throws Exception {
        // arrange
        String tenantConfigRoot = Paths.get("tenants").toAbsolutePath().toString();
        File file = createFile();
        Properties properties = createProperties();

        // act
        DataSource actual = null;
        try (MockedStatic<TenantFactory> tf = mockStatic(TenantFactory.class)) {
            tf.when(() -> TenantFactory.findByFileExtension(eq(Paths.get(tenantConfigRoot)), eq(".properties")))
                .thenReturn(Stream.of(file.toPath()));
            tf.when(() -> TenantFactory.loadProperties(eq(file), eq(this.encryptor)))
                .thenReturn(properties);
            tf.when(() -> TenantFactory.buildDataSource(eq(properties)))
                .thenReturn(createDefaultDataSource());

            tf.when(() -> TenantFactory.dataSource(any(), any(), any())).thenCallRealMethod();
            actual = TenantFactory.dataSource(createDefaultDataSource(), tenantConfigRoot, this.encryptor);
        }

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(TenantDataSource.class);
        assertThat(((TenantDataSource) actual).getResolvedDataSources())
            .hasSize(1)
            .containsKey("demo");
    }

    @Test
    void buildDataSource_PropertiesGiven_DriverManagerDataSourceReturned() throws Exception {
        // arrange
        Properties properties = createProperties();

        // act
        DataSource actual = TenantFactory.buildDataSource(properties);

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(DriverManagerDataSource.class);
    }

    @Test
    void loadProperties_FileExits_PropertiesReturned() throws Exception {
        // arrange
        File propertiesFile = createFile();

        FileOutputStream fos = new FileOutputStream(propertiesFile);
        createProperties().store(fos, "for test");
        // act
        Properties actual = null;
        try (MockedStatic<TenantFactory> tf = mockStatic(TenantFactory.class)) {
            tf.when(() -> TenantFactory.loadProperties(any(), any())).thenCallRealMethod();
            actual = TenantFactory.loadProperties(propertiesFile, this.encryptor);
        }

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty("id")).isEqualTo("demo");
    }

    @Test
    void findByFileExtension_FileExceptionMocked_ExceptionThrown() {
        // arrange
        Path path = Paths.get("tenants");

        // act
        Throwable actual;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.walk(path, 1)).thenThrow(new IOException());
            actual = catchThrowable(() -> TenantFactory.findByFileExtension(path, ".properties"));
        }

        // assert
        assertThat(actual).isInstanceOf(IOException.class);
    }

    @Test
    void loadProperties_FileExceptionMocked_ExceptionThrown() {
        // arrange
        File file = Paths.get("tenants", "test.properties").toFile();
        StandardPBEStringEncryptor encryptorMock = mock(StandardPBEStringEncryptor.class);

        // act
        Throwable actual;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.newInputStream(any())).thenThrow(new IOException());
            actual = catchThrowable(() -> TenantFactory.loadProperties(file, encryptorMock));
        }

        // assert
        assertThat(actual).isInstanceOf(IOException.class);
    }

    private File createFile() throws Exception {
        File result = File.createTempFile("demo", ".properties");
        result.deleteOnExit();

        Properties properties = new Properties();
        properties.put("id", "demo");
        properties.put("name", "demo");
        properties.put("type", "org.springframework.jdbc.datasource.DriverManagerDataSource");
        properties.put("spring.datasource.driver-class-name", "org.h2.Driver");
        properties.put("spring.datasource.url", "jdbc:h2:mem:tailoringexpert-demo;DB_CLOSE_DELAY=-1");
        properties.put("spring.datasource.username", "tailoringexpert_demo");
        properties.put("spring.datasource.password", "ENC(4qBa1ScLN/2lSdLpjRcdqnBtlN5zQrVW54n04C3f90U=)");

        properties.store(new FileOutputStream(result), "unittest");
        return result;
    }

    private Properties createProperties() {
        Properties result = new Properties();
        result.put("id", "demo");
        result.put("name", "demo");
        result.put("type", "org.springframework.jdbc.datasource.DriverManagerDataSource");
        result.put("spring.datasource.driver-class-name", "org.h2.Driver");
        result.put("spring.datasource.url", "jdbc:h2:mem:tailoringexpert-demo;DB_CLOSE_DELAY=-1");
        result.put("spring.datasource.username", "tailoringexpert_demo");
        result.put("spring.datasource.password", "ENC(4qBa1ScLN/2lSdLpjRcdqnBtlN5zQrVW54n04C3f90U=)");
        return result;
    }

    private DataSource createDefaultDataSource() {
        final DriverManagerDataSource result = new DriverManagerDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:tailoringexpert;DB_CLOSE_DELAY=-1");
        result.setUsername("tailoringexpert");
        result.setPassword("ENC(4qBa1ScLN/2lSdLpjRcdqnBtlN5zQrVW54n04C3f90U=)");
        return result;
    }
}
