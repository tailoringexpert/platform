# TailoringExpert

## About

| __Build Status__ | [![build](https://github.com/baedorf/tailoringexpert-plattform/actions/workflows/build.yml/badge.svg)](https://github.com/baedorf/tailoringexpert-plattform/actions/workflows/build.yml) |
|:-----------------| :--- |
| __Coverage__     | ![coverage](../badges/jacoco.svg) ![branches coverage](../badges/branches.svg) |
| __Source Info__  | [![License](https://img.shields.io/github/license/baedorf/tailoringexpert-plattform)](https://github.com/baedorf/tailoringexpert-plattform/blob/main/LICENSE) ![GitHub top language](https://img.shields.io/github/languages/top/baedorf/tailoringexpert-plattform) |

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

In case no data will be returned due unknown paths (project, tailoring, ...) _Optional_ shall be return.
RestController shall return _404_ in case of _Optional.empty_.

### Business/Domain core

Business core must not have external dependencies except 

* lombok
* mapstruct
* log4j2

### Data/Domain objects

Data/Domain objects of all layers shall be created in package _domain_.

## Persistence

Persistence objects and database access shall be implemented in _tailoring-data-jpa_.
Dataobjects shall be created in package _domain_, spring data acces in _repository_
OR-Mapping is to be implemented via annotations.


## Use Cases

![alt text](src/site/plantuml/usecases.svg "Usecases")

## Architecture documentation

Architecture documentation can be found here as [arc42](src/site/arc42/tailoringexpert.adoc) document.

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

## Deployment

![alternative text](https://www.plantuml.com/plantuml/proxy?cache=no&src=https://raw.github.com/baedorf/tailoringexpert-plattform/src/site/arc42/plantuml/DeploymentDocker.plantuml "Deployment")

## Build application

### Prerequisites

- Java 11 JDK
- Maven
    - profile containing liquibase properties for integration test

### Build

To build the plattform it is recommended to create a profile containing `liquibase` properties for intgegration test
database connection.

Plattform can be build using

> mvn -P tailoringexpert-plattform.local clean install

### Run

For testing purpose there is a sample plattform in module `tailoringexpert-integrationtest`.
See instructions for setting up and running the (demo) system in [README.md](tailoringexpert-integrationtest/README.md)

## Links

- [Schnittstellen Dokumentation (Swagger)](http://localhost:8080/swagger-ui.html#/)
- [Liquibase](https://www.liquibase.org/)
- [Lombok](https://projectlombok.org/)
- [Mapstruct](https://mapstruct.org/)
- [Plantuml](https://plantuml.com/)
- [dotenv-java](https://github.com/cdimascio/dotenv-java) 
