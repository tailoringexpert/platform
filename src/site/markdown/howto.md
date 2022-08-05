# Howto

## Log4j vs jul

Im nachfolgenden werden die Loglevel von Log4j und jul gegenüber gestellt.

| Log4j  |   jul   |
| ------ | -----   |
| OFF    |   OFF   |
| ERROR  | SEVERE  |
| WARN   | WARNING |
| INFO   |  INFO   |
| CONFIG | CONFIG  |
| DEBUG  |  FINE   |
| TRACE  |  FINER  |
| FINEST | FINEST  |
| ALL    |   ALL   |

## Datenbank

Ein Datenbanlk Dump kann im Projekt _tailoring-data-jpa_ mittels

    mvn exec:exec@dump -DskipTests

erstellt werden.\
Ohne Angabe eines Profiles wird die Verbindung zur Postgres DB verwendet