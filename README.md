[![Build Status](https://travis-ci.org/harvard-library/librarycloud_collections.png?branch=master)](https://travis-ci.org/harvard-library/librarycloud_collections)

[![Build Status](https://travis-ci.org/harvard-library/librarycloud_collections.png?branch=develop)](https://travis-ci.org/harvard-library/librarycloud_collections)

LibraryCloud Collections API
============================

The LibraryCloud Collections API provides access to metadata about groups of items in the Harvard Library collections, and allows creation and editing of new groups of items

# Installation

## Prerequisites

* Java 8
* Tomcat 8
* Maven

### Maven installation (RHEL)

    sudo yum install wget
    sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
    sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
    sudo yum install apache-maven

## Build and Deploy

### Download the code

    git clone https://github.com/harvard-library/librarycloud_collections.git

### Update environment specific configuration

    cp src/main/resources/librarycloud.collections.env.properties.example src/main/resources/librarycloud.collections.env.properties

Upate  ```librarycloud.collections.env``` with the AWS keys and SQS environment name to use. (The SQS environment sets the prefix that's added to all LibraryCloud queues)
```max_collections_per_user``` defines the maximum amount of collections that a user is allowed to create, and should be set to an integer of your choice.

### Database setup and migrations
Database connection settings can be configured in the following places:

    src/main/resources/librarycloud.collections.env.properties
    src/test/resources/librarycloud.collections.test.env.properties

To run database migrations:

    mvn initialize flyway:migrate

Note: if you are working with an existing database without a `flywheel_schema_history` table, you
will need to 'baseline' the database first.

### Build and deploy the application with Maven

Setup Tomcat users for deployment. Edit ```{TOMCAT8_PATH}/conf/tomcat-users.xml``` and add

    <role rolename="manager-gui"/>
    <role rolename="manager-script"/>
    <user username="admin" password="PASSWORD_HERE" roles="manager-gui,manager-script" />

Restart Tomcat

    sudo service tomcat restart

Place your tomcat configuration information with everything else in `src/main/resources/librarycloud.collections.env.properties`.

Place your database configuration information with everything else in `src/main/resources/librarycloud.collections.env.properties`.

Build and deploy the application

    mvn clean initialize tomcat7:deploy

The collections API will now be listening at http://SERVER:8080/v2/collections

### Setup authorized users

Install MySQL client.

### Testing app locally

To test that app is building properly and running
- set JAVA_HOME = openjdk8
- in ./src/main/resources copy example props file
- - `> cp librarycloud.collections.env.properties.example librarycloud.collections.env.properties`
- configure db info for dev instance, ask developer (you can also configure a mysql db but more work)
- run maven
- - `> mvn clean install`
- copy the war to local tomcat 8 (war should autoload if tomcat started)
- - `> cp target/collections.war /PATH-TO-TOMCAT/webapps/`
- check that dev collections load in browser
- - http://localhost:8080/collections/v2/collections

### Note on testing 20220926
- Running mvn clean install proves build, and one set of tests
- CollectionsDAO tests are currently set to @igonre for, need additional attention regarding setup of local mysql
