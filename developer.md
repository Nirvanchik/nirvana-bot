## How to build
This project is bi-tool. It supports Maven & Gradle.

### Maven
To compile:
```
mvn compile
```
To run tests:
```
mvn test
```
To build local distribution:
```
mvn package -DskipTests
```
To build production distribution:
TODO

### Gradle
To compile:
```
gradlew assemble
```
To run tests:
```
gradlew test
```
To build local distribution:
```
gradlew installDist
```
To build production distribution:
```
gradlew distZip
```

## How to run
### Run from IDE (Eclipse, Intellij Idea, etc.)
To run bot from your IDE you should first create run configuration.
Run required bot class (NirvanaBot, StatisticsBot, etc.) as Java application.
The bot should fail with "config.xml not found" error.
IDE should create run configuration and there you will add "config_development.xml"
(or any other _dev.xml) command line argument.
### Run from command line
See insructions above how to build "local distribution".
When it's built you can run directly any ``*.cmd`` file in the checkout folder.
Batch script (runbot.cmd) detects available distribution automatically and runs it.
If multiple distributions is built it will select one of them.
Currently the preferable one is Maven but this may be changed later.

## Code style
Code style is based on lightened Goodle checkstyle rules.
For how to install checkstyle precommit hook see here: [.checkstyle/README.MD](.checkstyle/README.MD)