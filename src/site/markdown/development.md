# Implementierungsrichtlinien

## Dependency Injection

*   Dependency Injection(DI) ist über Java Konfiguration vorzunehmen.
*   Autoconfiguration ist mit Ausnahme der Spring-Data-Repositories verboten
*   Alle Konfiguratonen sind im _tailoring-bootapp_-Projekt abzulegen
*   Jedes Paket bekommt seine eigne Konfiguration

## Webservices
*	Für Webservices wird der Code-First Ansatz verfolgt. Eine wadl ist nicht händisch zu erzeugen.
*	Webservices sind als RestController umzusetzen
*	Webservices sind mittels **OpenApi** Annotation im zu dokumentieren.

## Lombok

Zur Vermeidung der händsichen Implementierung von

*	getter
*	setter
*	equals
*	hashCode
*	toString

ist [Lombok](https://projectlombok.org "Lombok") zu verwenden.


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
