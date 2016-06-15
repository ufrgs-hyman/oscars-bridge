###OSCARS Bridge

Module for easy communication with [OSCARS](https://github.com/esnet/oscars) instances.

####Requirements

- Java 1.7+
- Maven 2+

####ESnet libs 

Currently that step is manual, enter the lib folder from the project root and execute:

```
mvn install:install-file -Dfile=oscars-client-0.0.1-SNAPSHOT.jar -DgroupId=net.es.oscars -Dversion=0.0.1-SNAPSHOT -DartifactId=OSCARS -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=common-soap-0.0.1-SNAPSHOT.jar -DgroupId=net.es.oscars -Dversion=0.0.1-SNAPSHOT -DartifactId=common-soap -Dpackaging=jar -DgeneratePom=true
```

####Usage

Start the web service:

```
mvn spring-boot:run
```

Access http://localhost:8080/oscars-bridge/circuits to get all active or future circuits from the configured OSCARS instance in JSON format.

####Development with Eclipse IDE

After Java and Maven setup, simply execute:

```
mvn eclipse:eclipse
```

All dependencies will be downloaded and your classpath will be ready for a import project in Eclipse IDE.
