# tailoringexpert-data-jpa

Module of database component using `spring-data-jpa`.
Database creation and management is realized using `liquibase`.

## Database

Configuration of database connection shall be done using maven profile.
Profile shall contain following `liquibase` properties:

```
<liquibase.username>DB_USER</liquibase.username>
<liquibase.password>DB_PASSWORD</liquibase.password>
<liquibase.driver>JDBC TREIBER KLASSE</liquibase.driver>
<liquibase.url>JDBC URL</liquibase.url>
```

### Create schema

Following an example for creating user and schema using MySQL/MariaDB:

```
CREATE USER 'tailoringexpert'@'localhost' IDENTIFIED BY 'test1234';
CREATE DATABASE TAILORINGEXPERT CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON TAILORINGEXPERT.* TO 'tailoringexpert'@'localhost';
```

Adapt user and schema to needed values.
It is recommended to create a different user for each schema to use!

### Update database

Update (creating) database can be done using maven.
There are several `exec` tasks defined to

* drop
* install
* update

the database

As before mentioned all needed connection parameters shall be defined in a dedicated maven profile.
Using such a profile an example of invoking maven looks like

```
mvn -P tailoringexpert-plattform.local -DskipTests install exec:exec@dropAll exec:exec@install exec:exec@update 
```

There is also an intellij run configuration provided in this module.

## (Example) Queries

This chapter can be used to provide useful example database queries.
