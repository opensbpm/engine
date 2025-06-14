# Getting started with OpenSBPM:engine

## Overview

**OpenSBPM:engine** is a modular Java-based project designed to execute business processes using the Subject-oriented 
Business Process Management (S-BPM) approach. It is built with **Java**, **Spring Boot**, and **Spring Data JPA**, 
enabling easy persistence to configurable relational databases.

The engine can be embedded in applications in two primary ways:

1. As an **embedded Java library**
2. As an **embedded REST API**

---

## 1. Embedding as a Java Library

The engine is modular and extensible, allowing seamless integration into existing applications.

### Key Modules

- **`api`**: Public API to interact with the engine programmatically.
- **`core`**: Core implementation of the engine.
- **`xmlmodel`**: Responsible for parsing and validating workflow models using the OpenSBPM XML schema.

### Configuration

To use the engine:

- Import `org.opensbpm.engine.core.EngineConfig` to load necessary configurations.
- The engine uses **Spring Data JPA** to persist states in a relational database.
    - Supported databases include **H2**, **PostgreSQL**, and **MariaDB**.

### User Management

> **Note:** The engine does **not** include user management or authentication. You must implement these features independently.

---

## 2. Embedding as a REST API

OpenSBPM:engine can also be used as a REST API, providing HTTP-based access to engine functionality.

### Key Modules

- **`rest-api`**: Contains the JAX-RS API definitions for the engine.
- **`rest-services`**: Implements the REST API using **Apache CXF** and **Spring Boot**.
- **`rest-client`**: Provides a simple JAX-RS client to consume the REST API.

### Frameworks Used

- **Apache CXF** for building JAX-RS RESTful services.
- **Spring Boot** for application configuration and startup.

### User Management

> **Note:** Like the library mode, the REST API **does not** include user management or authentication. These must be implemented as part of your integration.


# Maven Dependency
The releases are hosted in the GitHub Packages repository. To include the dependency in your Maven project, add the following
to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>opensbpm</id>
        <name>OpenSBPM Releases</name>
        <url>https://maven.pkg.github.com/opensbpm/engine</url>
    </repository>
</repositories>
```
You need to authenticate with GitHub Packages. You can do this by creating a personal access token with the `read:packages`

Use dependency management to import the `opensbpm-engine-bom` dependency.
The current latest release is `0.1.3`.

```xml
<dependencyManagment>
    <dependencies>
        <dependency>
            <groupId>org.opensbpm.engine</groupId>
            <artifactId>opensbpm-engine-bom</artifactId>
            <version>0.1.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagment>

<dependencies>
    <dependency>
        <groupId>org.opensbpm.engine</groupId>
        <artifactId>opensbpm-engine-core</artifactId>
    </dependency>
</dependencies>
```
