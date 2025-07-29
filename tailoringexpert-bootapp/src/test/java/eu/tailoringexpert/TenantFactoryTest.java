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

import lombok.extern.log4j.Log4j2;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.walk;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Log4j2
class TenantFactoryTest {

    StandardPBEStringEncryptor encryptor;
    DataSource defaultDataSourceMock;
    TenantFactory factory;

    @BeforeEach
    void beforeEach() {
        this.defaultDataSourceMock = mock(DataSource.class);
        String tenantConfigRoot = Paths.get("tenants").toAbsolutePath().toString();
        this.encryptor = new StandardPBEStringEncryptor();
        this.encryptor.setPassword("TailoringForDemo!");
        this.encryptor.setAlgorithm("PBEWithMD5AndTripleDES");

        this.factory = spy(new TenantFactory(this.defaultDataSourceMock, tenantConfigRoot, this.encryptor));
    }

    @AfterEach
    void afterEach() throws Exception {
        Field field = TenantContext.class.getDeclaredField("registeredTenants");
        field.setAccessible(true); //NOPMD - suppressed AvoidAccessibilityAlteration
        field.set(null, new HashMap<>());
        field.setAccessible(false);
    }

    @Test
    void tenants_ValidConfigRootNoPropertyFiles_EmptyMapReturned() {
        // arrange
        String tenantConfigRoot = Paths.get("tenants").toAbsolutePath().toString();

        doReturn(Stream.<Path>empty())
            .when(factory).findByFileExtension(Paths.get(tenantConfigRoot), ".properties");

        // act
        Map<String, String> actual = factory.tenants();

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void tenants_ValidConfigRootPropertyFileExists_MapWithTenantReturned() throws Exception {
        // arrange
        String tenantConfigRoot = Paths.get("tenants").toAbsolutePath().toString();
        File file = createFile();
        Properties properties = createProperties();

        doReturn(Stream.of(file.toPath()))
            .when(factory).findByFileExtension(Paths.get(tenantConfigRoot), ".properties");
        doReturn(properties)
            .when(factory).loadProperties(file);

        // act
        Map<String, String> actual = factory.tenants();

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

        doReturn(Stream.of(file.toPath()))
            .when(factory).findByFileExtension(Paths.get(tenantConfigRoot), ".properties");
        doReturn(properties)
            .when(factory).loadProperties(file);
        doReturn(createDefaultDataSource())
            .when(factory).buildDataSource(properties);

        // act
        DataSource actual = factory.dataSource();

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(TenantDataSource.class);
        assertThat(((TenantDataSource) actual).getResolvedDataSources())
            .hasSize(1)
            .containsKey("demo");
    }

    @Test
    void buildDataSource_PropertiesGiven_DriverManagerDataSourceReturned() {
        // arrange
        Properties properties = createProperties();

        // act
        DataSource actual = factory.buildDataSource(properties);

        // assert
        assertThat(actual)
            .isNotNull()
            .isInstanceOf(DriverManagerDataSource.class);
    }

    @Test
    void loadProperties_FileExits_PropertiesReturned() throws Exception {
        // arrange
        File propertiesFile = createFile();

        try (OutputStream fos = Files.newOutputStream(propertiesFile.toPath())) {
            createProperties().store(fos, "for test");
        }

        // act
        Properties actual = factory.loadProperties(propertiesFile);

        // assert
        assertThat(actual).isNotNull();
    }

    @Test
    void loadProperties_FileWithEnvVarExits_PropertiesWithResolvedEnvVarReturned() throws Exception {
        // arrange
        File propertiesFile = createFile();

        // act
        Properties actual = factory.loadProperties(propertiesFile);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty("ENV"))
            .isNotBlank()
            .isNotEqualTo("${PATH}");
    }

    @Test
    void loadProperties_FileWithSystemPropertyExits_PropertiesWithResolvedSystemPropertyReturned() throws Exception {
        // arrange
        System.setProperty("MY_PROP", "ENC(4qBa1ScLN/2lSdLpjRcdqnBtlN5zQrVW54n04C3f90U=");

        File propertiesFile = createFile();

        // act
        Properties actual = factory.loadProperties(propertiesFile);

        // assert
        assertThat(actual).isNotNull();
        assertThat(actual.getProperty("SYSTEM"))
            .isNotBlank()
            .isEqualTo("ENC(4qBa1ScLN/2lSdLpjRcdqnBtlN5zQrVW54n04C3f90U=");
    }


    @Test
    void findByFileExtension_FileExceptionMocked_ExceptionThrown() {
        // arrange
        Path path = Paths.get("tenants");

        // act
        Throwable actual;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> walk(path, 1)).thenThrow(new IOException());
            actual = catchThrowable(() -> factory.findByFileExtension(path, ".properties"));
        }

        // assert
        assertThat(actual).isInstanceOf(IOException.class);
    }

    @Test
    void loadProperties_FileExceptionMocked_ExceptionThrown() {
        // arrange
        File file = Paths.get("tenants", "test.properties").toFile();

        // act
        Throwable actual;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> newInputStream(any())).thenThrow(new IOException());
            actual = catchThrowable(() -> factory.loadProperties(file));
        }

        // assert
        assertThat(actual).isInstanceOf(IOException.class);
    }

    private File createFile() throws Exception {
        File result = File.createTempFile("demo", ".properties");
        result.deleteOnExit();

        Properties properties = createProperties();
        properties.store(Files.newOutputStream(result.toPath()), "unittest");

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
        result.put("ENV", "${PATH}");
        result.put("SYSTEM", "${MY_PROP}");

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
