# OpenSBPM:engine

*OpenSBPM:engine* is a free implementation of a Subject-Oriented Business Process Management ([S-BPM](https://en.wikipedia.org/wiki/Subject-oriented_business_process_management)) workflow engine. It 
serves as the core component for executing S-BPM models, utilizing Spring Boot Data JPA to persist states into a configurable 
database.

## Project Status

The engine is under active development and currently considered unstable. The public API is subject to change without 
prior notice. As of now, there are no known production deployments. If you plan to use this engine in a production environment, 
please create an issue in the repository to facilitate the creation of a stable release. For experimental purposes, snapshot 
JARs are available in the Maven Repository associated with this project.

To include the snapshot repository in your Maven project, add the following to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>opensbpm-release</id>
        <name>OpenSBPM Releases</name>
        <url>https://maven.pkg.github.com/opensbpm/engine</url>
    </repository>
</repositories>
```

## Getting Started

To get started with OpenSBPM:engine, you can refer to the "Get Started" guide available on the official website: 
([OpenSBPM](https://www.opensbpm.org/getstarted))

## Features

- **Subject-Oriented Modeling**: Facilitates the creation and execution of S-BPM models.
- **Database Persistence**: Utilizes [SpringBoot Data JPA](https://spring.io/projects/spring-data-jpa) for state persistence in configurable databases.
- **Extensible Architecture**: Designed for easy integration and extension to fit various business needs.

## Contributing

Contributions are welcome! If you're interested in contributing to the development of OpenSBPM:engine, please fork the repository 
and submit a pull request. For major changes, it's advisable to open an issue first to discuss the proposed modifications.

## License

This project is licensed under the GNU General Public License v3.0. For more details, see the [LICENSE](LICENSE) file.

## Contact

For more information, visit the official website: ([OpenSBPM](https://opensbpm.org/))

For issues or inquiries, please open an issue in the GitHub repository.
