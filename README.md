# Tailoring

## About

| __Build
Status__
| [![build](https://github.com/baedorf/tailoringexpert-plattform/actions/workflows/build.yml/badge.svg)](https://github.com/baedorf/tailoringexpert-plattform/actions/workflows/build.yml)
|
| :--- | :--- |
| __Coverage__ | ![coverage](../badges/jacoco.svg) ![branches coverage](../badges/branches.svg) |
| __Source
Info__
| [![License](https://img.shields.io/github/license/baedorf/tailoringexpert-plattform)](https://github.com/baedorf/tailoringexpert-plattform/blob/main/LICENSE) ![GitHub top language](https://img.shields.io/github/languages/top/baedorf/tailoringexpert-plattform)
|

## Development policies

Es gelten die [Entwicklungsrichtlinien](src/site/markdown/development.md "Entwicklungsrichtlinien").

## Use Cases

![alt text](src/site/plantuml/usecases.svg "Usecases")

## Module

### Maven Modul-Struktur

| Modulname                | Beschreibung                                                                                                                 |
|:-------------------------|:-----------------------------------------------------------------------------------------------------------------------------|
| tailoringexpert-core     | Business core of plattform.                                                                                                  |
| tailoringexpert-data-jpa | Implementation of data access needed by core component                                                                       |
| tailoringexpert-rest     | Component providing REST services of plattform                                                                               |
| tailoringexpert-poi      | Module for importing requirements and generating Excel files using POI.                                                      | 
| tailoringexpert-openhtmltopdf   | Module for generating PDF documents using  Openhtmltop.                                                                      | 
| tailoringexpert-security | Security configuration using spring-security without using an external identity manager                                      |
| tailoringexpert-tenant   | Proxies for tenant implementations of type TenantInterface. |

### Level 0

![alternative text](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/baedorf/taloringexpert-plattform/src/site/arc42/plantuml/level0.plantuml "Level 0")

## Level 1

![alternative text](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/baedorf/taloringexpert-plattform/src/site/arc42/plantuml/TailoringWhitebox.plantuml "Level 1")

### Verwendete Libraries

Libraries used by the plattform are (pre) defined in tailoringexpert-dependencies.
Main libraries used are

* lombok
* mapstruct
* log4j2
* Spring boot
  * data-jpa
  * hateoas
  * cache
  * actuator
  * security
  * thymeleaf
  * security
  * tomcat
* Apache POI
* openhtml2pdf

## Deployment

![alternative text](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/baedorf/tailoringexpert-plattform/src/site/arc42/plantuml/DeploymentDocker.plantuml "Deployment")

## Bauen des Anwendung

### Voraussetzungen

- Java 11 JDK
- Maven

### Build

Ohne Angabe eines Profiles werden mittels

> mvn clean install

## Vorbereitung Datenbank

### Voraussetzung

Es muss ein Datenbankschema mit einem Benutzer vorhanden sein

#### Schema anlagen

```
CREATE USER 'tailoringexpert'@'%' IDENTIFIED BY 'test1234';
CREATE DATABASE TAILORINGEXPERT CHARACTER SET utf8mb4;
GRANT ALL PRIVILEGES ON TAILORINGEXPERT.* TO 'tailoringexpert'@'%;'
```

#### Tabellenstrukturen erstellen/aktualisieren

Die Datenbankskripte liegen in Form von _liquibase_ XML-Dateien im Projekt _tailoringexpert-data-jpa_ vor.
Die Tabellen und Contraints werden mittels

> mvn -P develop install exec:exec@dropAll exec:exec@install exec:exec@update

auf die Datenbank angewendet.
Für die Anlage des Datenbanks ist unter

    tailoringexpert-data-jpa/src/assembly/runConfigurations

eine Intellij Run-Configuration vorhanden.

### Basiskatalog

Für die Entwicklung existeriert ein Katalog in Form einer importierbaren JSON Datei.
Dieser kann z.B. mittel Postman eingespielt werden. Dafür muss die Plattform und ein Beispielmandant gestartet werden.

## Starten der Anwendung

Beim TailoringExpert handelt sich es um eine mandantenfähige Spring Boot Anwendung.
Die Haupt-/Starterklasse ist

> eu.tailoringexpert.App

und kann mittels java eu.tailoringexpert.App gestartet werden

Jeder Mandant muss seine Konfiguration in dem Paket

> eu.tailoringexpert

anlegen. Nur so kann diese beim Start der Plattform angezogen werden.

## Links

- [Schnittstellen Dokumentation (Swagger)](http://localhost:8080/swagger-ui.html#/)
- [Liquibase](https://www.liquibase.org/)
- [Lombok](https://projectlombok.org/)
- [Mapstruct](https://mapstruct.org/)
- [Plantuml](https://plantuml.com/)
