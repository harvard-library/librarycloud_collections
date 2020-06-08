[![Build Status](https://travis-ci.org/harvard-library/librarycloud_collections.png?branch=master)](https://travis-ci.org/harvard-library/librarycloud_collections)

[![Build Status](https://travis-ci.org/harvard-library/librarycloud_collections.png?branch=develop)](https://travis-ci.org/harvard-library/librarycloud_collections)

LibraryCloud Collections API
============================

The LibraryCloud Collections API provides access to metadata about groups of items in the Harvard Library collections, and allows creation and editing of new groups of items

# Installation

## Prerequisites

* Java 7
* Tomcat
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

Setup Tomcat users for deployment. Edit ```{TOMCAT7_PATH}/conf/tomcat-users.xml``` and add

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
