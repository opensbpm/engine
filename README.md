# OpenSBPM:engine
*OpenSBPM:engine* is a free [S-BPM](https://en.wikipedia.org/wiki/Subject-oriented_business_process_management) workflow engine implementation.

*OpenSBPM:engine* is the core component to execute S-BPM models. It uses [SpringBoot Data JPA](https://spring.io/projects/spring-data-jpa) 
to persist the states into a configurable database.

## Project Status
The engine is under active development and currently unstable. For now the 
public API can change without further notice. Currently there are no known 
production usages, so if you are planning to use this engine in production 
create a issue and a release will be created.\\
For experimental usage snapshot JARs are available in Maven Repository of this repo.

```xml
<repositories>
    <repository>
        <id>opensbpm-release</id>
        <name>OpenSBPM Releases</name>
        <url>https://maven.pkg.github.com/opensbpm/engine</url>
    </repository>
</repositories>
```
