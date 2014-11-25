[![Build Status](https://travis-ci.org/harvard-library/librarycloud_collections.png?branch=master)](https://travis-ci.org/harvard-library/librarycloud_collections)

LibraryCloud Collections API
============================

The LibraryCloud Collections API provides access to metadata about groups of items in the Harvard Library collections, and allows creation and editing of new groups of items

# Installation

## Prerequisites

* Java 7
* Tomcat
* Maven

## Build and Deploy

### Download the code

    git clone git@github.com:harvard-library/librarycloud_collections.git

### Update environment specific configuration

    cp src/main/resources/librarycloud.collections.env.properties src/main/resources/librarycloud.collections.env

Upate  ```librarycloud.collections.env``` with the AWS keys and SQS environment name to use. (The SQS environment sets the prefix that's added to all LibraryCloud queues)

### Build the application with Maven

    mvn clean tomcat7:deploy

