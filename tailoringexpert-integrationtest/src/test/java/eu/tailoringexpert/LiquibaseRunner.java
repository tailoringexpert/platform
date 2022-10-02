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

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

import javax.sql.DataSource;
import java.sql.Connection;

import static java.util.stream.IntStream.range;


@AllArgsConstructor
public class LiquibaseRunner {

    @NonNull
    private DataSource dataSource;

    @SneakyThrows
    public void dropAll() {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            new Liquibase((String) null, new ClassLoaderResourceAccessor(), database).dropAll();
        }
    }

    public void runChangelog(String... changeLogFiles) {
        range(0, changeLogFiles.length)
                .forEachOrdered(i -> execute(changeLogFiles[i]));
    }

    @SneakyThrows
    private void execute(String changeLogFile) {
        System.setProperty("liquibase.secureParsing", "false");
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            new Liquibase(changeLogFile, new ClassLoaderResourceAccessor(), database).update("test");
        }
    }
}
