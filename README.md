##OSCARS Bridge

Module for easy communication with [OSCARS](https://github.com/esnet/oscars) instances.

###Usage

####Option 1: Download builded JAR file

1.1. Download builded JAR from [here](http://meican-cipo.inf.ufrgs.br/playground/oscars-bridge-2.0.1.jar)

1.2. Start the application

Starting the app in background:

```
java -jar oscars-bridge-2.0.1.jar --url=https://localhost:9001/OSCARS/ --ok=/path/to/oscars.jks --ou=user --op=pass --lk=/path/to/localhost.jks --lp=pass > /var/log/bridge.log 2>&1 &
```

Application required parameters

- ok - full path to OSCARS Keystore
- ou - OSCARS Keystore user
- op - OSCARS Keystore password
- lk - full path to localhost Keystore
- lp - localhost Keystore password

Access http://localhost:8080/oscars-bridge/circuits to get all active or future circuits from the configured OSCARS instance in JSON format.

####Option 2: Build your JAR from source

Requirements for build

- Java 1.6+
- Maven 2+
- OSCARS libs

2.1. Install required OSCARS libs 

Currently that step is manual, enter the lib folder from the project root and execute:

```
mvn install:install-file -Dfile=oscars-client-0.0.1-SNAPSHOT.jar -DgroupId=net.es.oscars -Dversion=0.0.1-SNAPSHOT -DartifactId=OSCARS -Dpackaging=jar -DgeneratePom=true
mvn install:install-file -Dfile=common-soap-0.0.1-SNAPSHOT.jar -DgroupId=net.es.oscars -Dversion=0.0.1-SNAPSHOT -DartifactId=common-soap -Dpackaging=jar -DgeneratePom=true
```

2.2. Build JAR

```
mvn install
```

2.3. Start application

Follow instruction on the step 1.2.

###Development with Eclipse IDE

After Java and Maven setup, simply execute:

```
mvn eclipse:eclipse
```

All dependencies will be downloaded and your classpath will be ready for a import project in Eclipse IDE.
