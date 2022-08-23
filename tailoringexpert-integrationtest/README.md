# tailoringexpert-arzs-integrationstest

Modul für Integrationstest und Start der Plattform für den Mandanten *arzs*.

## Plattform Konfiguration

Am einfachsten ist die Konfiguration über einen _XAMP_ Stack.
Dieser kann als portable Version von [Apache Friends](https://www.apachefriends.org/de/index.html) geladen werden.

### Installation

Das Zip ist in ein beliebiges Verzeichnis zu entpacken.
Bei einer Windows Umgebung ist im Root Verzeichnis zuerst die Batchdatei _setup_xampp.bat_ auszuführen.
Hiermit werden die entsprechenden Pfade in der Konfigurationsdateien geschrieben.

#### Apache Webserver

Um auf die Bilder für den Katalog über den Webserver zugreifen zu können ist ein *vhost* anzulegen.
Ein Beispiel hierfür ist unter 

> src/assembly/localhost/apache/conf/extra/httpd_vhost.conf

gegeben.
Dieses Beispiel ist ab *apache* in das Verzeichnis **%XAMPP_HOME%/apache/conf/extra** zu kopieren und entsprechend anzupassen

#### MariaDB Datenbank

Vor dem ersten Start der Datenbank sollte noch das Encoding auf **UTF 8** geändert werden.
Dafür ist die Datei

> %XAMPP_HOME%/mysql/bin/my.conf

im Bereich der **UTF 8 Settings** anzupassen

    ## UTF 8 Settings
    #init-connect=\'SET NAMES utf8\'
    collation_server=utf8_unicode_ci
    character_set_server=utf8
    skip-character-set-client-handshake
    #character_sets-dir="/Users/baed_mi/entwicklung/seu/xamp/xampp-portable-windows-x64-8.1.6-0-VS16/mysql/share/charsets"
    sql_mode=NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION
    log_bin_trust_function_creators = 1

Der Administrator hat den Login _*root*_ und **KEIN** Passwort!

    CREATE USER 'tailoringexpert_plattform'@'localhost' IDENTIFIED BY 'test1234';
    CREATE DATABASE TAILORINGEXPERT_PLATTFORM CHARACTER SET utf8mb4;
    GRANT ALL PRIVILEGES ON TAILORINGEXPERT_PLATTFORM.* TO 'tailoringexpert_plattform'@'localhost';

### Start

Am einfachsten ist der Start mit der Anwendung _xampp-control.exe_. Hier lassen sich die einzelnen Module 
(Webserver, Datenbank) komfortabel starten und stoppen.


