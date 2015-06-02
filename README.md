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

### Build and deploy the application with Maven

Setup Tomcat users for deployment. Edit ```{TOMCAT7_PATH}/conf/tomcat-users.xml``` and add

    <role rolename="manager-gui"/>
    <role rolename="manager-script"/>
    <user username="admin" password="PASSWORD_HERE" roles="manager-gui,manager-script" />

Restart Tomcat

    sudo service tomcat restart

Setup Maven to use this authentication information. Edit ```{MAVEN_PATH}/conf/settings.xml```

    <server>
        <id>TomcatServer</id>
        <username>admin</username>
        <password>PASSWORD_HERE</password>
    </server>

Update ```src/main/resources/META-INF/persistence.xml``` to have the correct database address, username, and password

Build and deploy the application

    mvn clean tomcat7:deploy

The collections API will now be listening at http://SERVER:8080/v2/collections

### Setup authorized users

Install MySQL client. 

