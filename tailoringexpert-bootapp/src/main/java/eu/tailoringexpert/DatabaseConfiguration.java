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

import lombok.NonNull;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@EnableJpaRepositories
public class DatabaseConfiguration {

    @Bean
    String dbconfigRoot(@NonNull @Value("${dbconfigRoot}") String dbconfigRoot) {
        return dbconfigRoot;
    }

    @Bean
    DataSource tenantDataSource(
        @NonNull @Value("${spring.datasource.driver-class-name}") String driverClassName,
        @NonNull @Value("${spring.datasource.url}") String url,
        @NonNull @Value("${spring.datasource.username}") String username,
        @NonNull @Value("${spring.datasource.password}") String password,
        @NonNull @Qualifier("dbconfigRoot") String dbconfigRoot,
        @NonNull @Qualifier("encryptorBean") StringEncryptor encryptor) throws IOException {
        DataSource defaultDataSource = dataSource(driverClassName, url, username, password);
        return TenantFactory.dataSource(defaultDataSource, dbconfigRoot, encryptor);
    }

    private DataSource dataSource(String driverClassName, String url, String username, String password) {
        final DriverManagerDataSource result = new DriverManagerDataSource(url, username, password);
        result.setDriverClassName(driverClassName);
        return result;
    }

    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
        @NonNull DataSource dataSource,
        @NonNull JpaVendorAdapter jpaVendorAdapter) {
        final LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setJpaVendorAdapter(jpaVendorAdapter);
        result.setPackagesToScan("eu.tailoringexpert");
        result.setDataSource(dataSource);
        return result;
    }


    @Bean
    JpaVendorAdapter jpaAdapter(
        @Value("${spring.jpa.generate-ddl}") boolean generateDdl,
        @Value("${spring.jpa.show-sql}") boolean showSql,
        @Value("${spring.jpa.database-platform}") String platform) {
        final HibernateJpaVendorAdapter result = new HibernateJpaVendorAdapter();
        result.setGenerateDdl(generateDdl);
        result.setShowSql(showSql);
        result.setDatabasePlatform(platform);
        return result;
    }

    @Bean
    PlatformTransactionManager transactionManager(
        @NonNull EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
