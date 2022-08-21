# tailoringexpert-data-jpa

Modul der Datenbankschiicht des Tailoringsystems unter Verwendung von spring-data-jpa.

## Datenbank 

Für die Konfiguration der Datenbankverbindung sollten Profile definiert werden.
Diese müssen nachfolgede liquibase Attribute enthalten:
```
<liquibase.username>DB_USER</liquibase.username>
<liquibase.password>DB_PASSWORD</liquibase.password>
<liquibase.driver>JDBC TREIBER KLASSE</liquibase.driver>
<liquibase.url>JDBC URL</liquibase.url>
```

### Schema anlagen
```
CREATE USER 'developer'@'%' IDENTIFIED BY 'test1234';
CREATE DATABASE TAILORINGEXPERT_ARZS CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON TAILORING_ARZS.* TO 'developer'@'%;'
```

### Datenbank aktualisieren
```
cd C:\Users\baed_mi\entwicklung\baed_mi\git\dlr\tailoringexpert\tailoringexpert-data-jpa
mvn -DskipTests -P develop install exec:exec@dropAll exec:exec@install exec:exec@update
```

## Abfragen

### Identifikatoren der aller Anforderungen eines Kapitels
```
select * from identifier where ANFORDERUNG_ID in (select anforderung_id from anforderungdefinition where ANFORDERUNGGRUPPE_ID = (select ANFORDERUNGGRUPPE_ID from anforderungdefinitiongruppe where KAPITEL='4.11.2.13'));
```