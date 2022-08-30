# Development policies

## Dependency Injection

* Dependency Injection(DI) has to be performed via Java configuration
* Autoconfiguration is forbidden except for Spring-Data-Repositories
* All configurations shall be implemented in _tailoringexpert-bootapp_-module
* Any package shall have a dedicated configuration class

## Webservices
*	Für Webservices wird der Code-First Ansatz verfolgt. Eine wadl ist nicht händisch zu erzeugen.
*	Webservices sind als RestController umzusetzen
*	Webservices sind mittels **OpenApi** Annotation im zu dokumentieren.

## Lombok

To avoid manual implementing of

*	getter
*	setter
*	equals
*	hashCode
*	toString

[Lombok](https://projectlombok.org "Lombok") shall be used.


## Validierungen und Exceptionhandling

Es soll vermehrt mit _Optional_ gearbeitet werden. In RestControllern soll
bei _Optional.empty_ eine _404_ zurück gegeben werden.

## Fachlicher Kern

Die Daten-/Domänen Objekte des fachlichen Kerns (_tailoring_core_) sind im
Paket _domain_ zu erstellen.
Der fachliche Kern darf __keine__ Library Abhängigkeit ausser zu _Mapstruct_
und _javax.inject_ besitzen!
Das Logging soll über dan Java Logger erfolgen.

## Persistenz

Die persistenten Objekte sind in der Komponente _tailoring-data-jpa_ im Paket _domain_ zu erstellen.
Das OR-Mapping erfolgt über Annotationen.

Der direkte Zugriff auf die Datenbank soll mittels Spring Data JPA Repositories  
umgesetzt werden.

# Dokumentationsrichtlinien

*	Die Dokumentationssprache ist deutsch.
*	Für jede Schnittstellen Methode ist gültiges javadoc zu erstellen.
*	Webservice Schnittstellenn sind vollständig mit OpenApi Annotationen zu dokumentieren.
