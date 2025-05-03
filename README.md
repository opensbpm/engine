# OpenSBPM:engine

*OpenSBPM:engine* is a free implementation of a Subject-Oriented Business Process Management ([S-BPM](https://en.wikipedia.org/wiki/Subject-oriented_business_process_management)) workflow engine. It 
serves as the core component for executing S-BPM models, utilizing Spring Boot Data JPA to persist states into a configurable 
database.


## Project Status

The engine is under active development and currently considered unstable. The public API is subject to change without 
prior notice. As of now, there are no known production deployments. If you plan to use this engine in a production environment, 
please create an issue in the repository to facilitate the creation of a stable release. For experimental purposes, snapshot 
JARs are available in the Maven Repository associated with this project.


## Getting Started

To run the sample application and end-to-end (E2E) tests, ensure Docker and Docker Compose are installed on your machine. 
Follow these steps:

1. Build the OCI containers and register the images in your local Docker registry:
```bash
mvn -pl sample-e2e,sample-app spring-boot:build-image
```

2. Run the E2E tests using the docker-compose.yml file in the sample-e2e module:
```bash
docker-compose -f sample-e2e/docker-compose.yml up --abort-on-container-exit
```
The E2E tests will start the sample-app and use sample-e2e user bots to initiate a workflow. The containers will stop 
automatically after the tests complete.


## Usage

OpenSBPM:engine can be used in two main ways:

### 1. As a Java Library

Use the `core` module in your own Java project. See [Library Usage](docs/library-usage.md).

### 2. As a REST API (Spring Boot App)

Add the `rest-services` module to your Spring Boot application. This module provides a REST API for interacting with the engine.
This module provides a REST API for the engine,
allowing you to interact with it over HTTP. You can use it to start workflows, manage tasks, and more.
See [Spring Boot Usage](docs/springboot-usage.md).


### Maven Dependency
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

Use dependency management to import the `opensbpm-engine-core` dependency.
The current latest release is `0.1.1`.

```xml
<dependencyManagment>
    <dependencies>
        <dependency>
            <groupId>org.opensbpm.engine</groupId>
            <artifactId>opensbpm-engine-bom</artifactId>
            <version>0.1.1</version>
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

You can find additional examples in https://github.com/opensbpm/opensbpm repository. 


## Contributing

Contributions are welcome! If you're interested in contributing to the development of OpenSBPM:engine, please fork the repository 
and submit a pull request. For major changes, it's advisable to open an issue first to discuss the proposed modifications.

## License

This project is licensed under the GNU General Public License v3.0. For more details, see the [LICENSE](LICENSE) file.

## Contact

For more information, visit the official website: ([OpenSBPM](https://opensbpm.org/))

For issues or inquiries, please open an issue in the GitHub repository.
