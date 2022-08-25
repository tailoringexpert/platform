# tailoringexpert-integrationstest

Module for integration testing and running a demo plattform.

## Limitations

All tenant specific interfaces are implemented in a very simple way to fulfill plattform requirements.
Implementations are to be understand as _non productive_ examples!

## System-Plattform configuration

The easiest way to configure the demo plattform is using a _XAMP_ stack.
The stack can be downloaded as portable version from [Apache Friends](https://www.apachefriends.org/de/index.html) .

### Installation

Unzip file in any directory.
In case of a Windows envitrionment you first have to run batchfile _setup_xampp.bat_.
This script will configure paths and writes them to the corresponding configuration files.

#### Apache Webserver

To access catalog pictures using the apache httpd websever it is required to create a *vhost*.
An example is given at

> src/assembly/localhost/apache/conf/extra/httpd_vhost.conf

This file has to be copied starting from *apache* to directory  **%XAMPP_HOME%/apache/conf/extra**.
After that change directories to system needs.

#### MariaDB Datenbank

Before first run of database encoding shall be changed to **UTF 8** geändert.
An example is file

> %XAMPP_HOME%/mysql/bin/my.conf

Section of **UTF 8 Settings** has to be changed to

    ## UTF 8 Settings
    #init-connect=\'SET NAMES utf8\'
    collation_server=utf8_unicode_ci
    character_set_server=utf8
    skip-character-set-client-handshake
    #character_sets-dir="/Users/baed_mi/entwicklung/seu/xamp/xampp-portable-windows-x64-8.1.6-0-VS16/mysql/share/charsets"
    sql_mode=NO_ZERO_IN_DATE,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION
    log_bin_trust_function_creators = 1

Admin account _*root*_ is **passwordless**!

Following commands can be used to create user, database and grant permissions:

    CREATE USER 'tailoringexpert_plattform'@'localhost' IDENTIFIED BY 'test1234';
    CREATE DATABASE TAILORINGEXPERT_PLATTFORM CHARACTER SET utf8mb4;
    GRANT ALL PRIVILEGES ON TAILORINGEXPERT_PLATTFORM.* TO 'tailoringexpert_plattform'@'localhost';

### Start

The easiest way to start webserver and database is using _xampp-control.exe_ app.

