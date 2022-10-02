# tailoringexpert-integrationstest

Module for integration testing and running a demo plattform.

## Test configuration

For user/system dependend variables the [dotenv-java](https://github.com/cdimascio/dotenv-java) library is used.
Copy file _.env.example_ to _.env_ and adapt setting to your local system. An automated build environment shall use
default values, which have to be defined in test classes if needed.

## Limitations

All tenant specific interfaces are implemented in a very simple way to fulfill plattform requirements.
Implementations are to be understand as _non productive_ examples!

## System-Plattform configuration

The easiest way to configure the demo plattform is using a _XAMP_ stack.
The stack can be downloaded as portable version from [Apache Friends](https://www.apachefriends.org/de/index.html) .

### Installation

Unzip file in any directory.
In case of a Windows environment you first have to run batchfile _setup_xampp.bat_.
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

### Database

To create/update database for plattform tenant, there are several maven exec tasks definded in `pom.xml`.
There is also a prepared intellij launch configuration. For general approch, please see `README.md` in 
module `tailoringexpert-data-jpa".

### Plattform
To run the application create a run configuration in intellij using `App.class` and add `.env` file.

### Base catalog
Use a rest client to upload an initial base catalog.
An example base catalog can be found [here](src/test/resources/basecatalog.json).
It is necessary to add  header attribute `X-Tenant` with value `plattform`. 

### Web
In project `tailoringexpert-web` contains a `.env.development.local.example` file. Copy this to `.env.development.local`
and set `VUE_APP_TENANT=plattform`.



