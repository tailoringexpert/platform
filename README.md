# TailoringExpert

## About

| __Build Status__ | [![build](https://github.com/tailoringexpert/plattform/actions/workflows/build.yml/badge.svg)](https://github.com/tailoringexpert/plattform/actions/workflows/build.yml) |
|:-----------------| :--- |
| __Coverage__     | ![coverage](../badges/jacoco.svg) ![branches coverage](../badges/branches.svg) |
| __Source Info__  | [![License](https://img.shields.io/github/license/tailoringexpert/plattform)](https://github.com/tailoringexpert/plattform/blob/main/LICENSE) ![GitHub top language](https://img.shields.io/github/languages/top/tailoringexpert/plattform) |

## Development policies

### Dependency Injection

Tailoringexport plattform is realized using Spring. Following rules shall be followed:

* Dependency Injection(DI) has to be performed via Java configuration
* autoconfiguration is forbidden except for Spring-Data-Repositories
* all configurations shall be implemented in _tailoringexpert-bootapp_-module
* any package shall have a dedicated configuration class

### Webservices
*	Code first approach, no manual created webservice descriptor.
*	Webservices are to be implemented as `RestController` in `tailoringexpert-rest` module
*	Webservices are to be documented using **OpenApi** annotations

### Lombok

To avoid manual implementing of

*	getter
*	setter
*	equals
*	hashCode
*	toString

[Lombok](https://projectlombok.org "Lombok") shall be used.

### Datatype mapping

To reduce manual effort for converting different data types, [Mapstruct](https://mapstruct.org/) shall be used.
Each service which needs datatype conversation shall define a mapper class named 
  
> ServiceNameMapper

in same package as service.

### Validation and Exceptionhandling

In case no data will be returned due unknown paths (project, tailoring, ...) _Optional_ shall be returned.
RestController shall return _404_ in case of _Optional.empty_.

### Business/Domain core

Business core must not have external dependencies except 

* lombok
* mapstruct
* log4j2

### Data/Domain objects

Data/Domain objects of all layers shall be created in package _domain_.

## Persistence

Persistence objects and database access shall be implemented in _tailoringexpert-data-jpa_.
Dataobjects shall be created in package _domain_, spring data acces in _repository_
OR-Mapping is to be implemented via annotations.


## Use Cases

![alternative text](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/tailoringexpert/plattform/main/src/site/arc42/plantuml/Usecases.plantuml "Usecases")

## Architecture documentation

Architecture documentation can be found here as [arc42](src/site/arc42/tailoringexpert.adoc) document.

## Module

### Maven Modul-Struktur

| Modulname                     | Beschreibung                                                                                                                |
|:------------------------------|:----------------------------------------------------------------------------------------------------------------------------|
| tailoringexpert-core          | Business core of platform.                                                                                                  |
| tailoringexpert-data-jpa      | Implementation of data access needed by core component                                                                      |
| tailoringexpert-rest          | Component providing REST services of plattform                                                                              |
| tailoringexpert-poi           | Module for importing requirements and generating Excel files using POI.                                                     | 
| tailoringexpert-openhtmltopdf | Module for generating PDF documents using  Openhtmltop.                                                                     | 
| tailoringexpert-security      | Security configuration using spring-security without using an external identity manager                                     |
| tailoringexpert-tenant        | Proxies for tenant implementations of type TenantInterface.                                                                 |
| tailoringexpert-bootapp       | Runnable app of platform                                                                                                    |
| tailoringexpert-distribution | Module to create archive to be used for creating a platform docker image |
### Level 0

![alternative text](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/tailoringexpert/plattform/main/src/site/arc42/plantuml/level0.plantuml "Level 0")

## Level 1

![alternative text](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/tailoringexpert/plattform/main/src/site/arc42/plantuml/TailoringWhitebox.plantuml "Level 1")

## Deployment

![alternative text](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/tailoringexpert/plattform/main/src/site/arc42/plantuml/DeploymentDocker.plantuml "Deployment")

## Build application

### Prerequisites

- Java 17 JDK
- Maven

### Build

To build the plattform it is recommended to create a profile containing `liquibase` properties for intgegration test
database connection.

Plattform can be build using

> mvn -P tailoringexpert-plattform.local clean install

### Run

For testing purpose there is a demo plattform in [github](https://github.com/tailoringexpert/demo).
See instructions for setting up and running the (demo) system in [README.md](tailoringexpert-integrationtest/README.md)

## Encrypting credentials (for platform boot-app)
There is a commandline client _eu.tailoringexpert.JasyptClient_ in _tailoringxpert-bootapp_ module.

Usage:
> eu.tailoringexpert.JasyptClient --algorithm PBEWithMD5AndTripleDES --password ThisIsUsedToEncryptParameters --parameter param1 --parameter paramX

If no algorithm was provided, PBEWithMD5AndTripleDES is used as default algorithm, which is also used by the bootapp.

## Links

- [Schnittstellen Dokumentation (Swagger)](http://localhost:8080/swagger-ui.html#/)
- [Liquibase](https://www.liquibase.org/)
- [Lombok](https://projectlombok.org/)
- [Mapstruct](https://mapstruct.org/)
- [Plantuml](https://plantuml.com/)
- [dotenv-java](https://github.com/cdimascio/dotenv-java) 
