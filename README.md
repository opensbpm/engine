# OpenSBPM:engine

*OpenSBPM:engine* is a free implementation of a Subject-Oriented Business Process Management ([S-BPM](https://en.wikipedia.org/wiki/Subject-oriented_business_process_management)) workflow engine. It 
serves as the core component for executing S-BPM models, utilizing Spring Boot Data JPA to persist states into a configurable 
database.


## Project Status

The engine is under active development and currently considered unstable. The public API is subject to change without 
prior notice. As of now, there are no known production deployments. If you plan to use this engine in a production environment, 
please create an issue in the repository to facilitate the creation of a stable release. For experimental purposes, snapshot 
JARs are available in the Maven Repository associated with this project.

## Documentation

You can find detailed design and architecture documentation in the [docs folder](docs/index.md)

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

### Testing the Sample Application
To test the sample application, you can use the following steps:
1. Start the sample application using docker and the image built in the previous step:
```bash
docker run opensbpm/sample-app:latest  
```

Alternatively, you can run the sample application using Maven:
```bash
mvn -pl sample-app spring-boot:run
```

2. You can fetch the openapi specification for the REST API using http://localhost:8080/v3/api-docs/swagger-config

3. Swagger UI is available at http://localhost:8080/services/api-docs/
4. Explore '/v3/api-docs/public' to get info about the login endpoint.
 

## Usage

OpenSBPM:engine can be used in two main ways:

1. As an **embedded Java library**: See [Library Usage](docs/getting-started.md#1-embedding-as-a-java-library).

2. As an **embedded REST API**: See [REST API Usage](docs/getting-started.md#2-embedding-as-a-rest-api).


### Maven Dependency
The releases are hosted in the GitHub Packages repository. To include the dependency in your Maven project, follow the
Maven Dependency instructions in [Maven Dependency Usage](docs/getting-started.md#maven-dependency).

You can find additional examples in https://github.com/opensbpm/opensbpm repository. 


## Contributing

Contributions are welcome! If you're interested in contributing to the development of OpenSBPM:engine, please fork the repository 
and submit a pull request. For major changes, it's advisable to open an issue first to discuss the proposed modifications.

## License

This project is licensed under the GNU General Public License v3.0. For more details, see the [LICENSE](LICENSE) file.

## Contact

For more information, visit the official website: ([OpenSBPM](https://opensbpm.org/))

For issues or inquiries, please open an issue in the GitHub repository.
