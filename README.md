# JdbcMsSql - SIARD 2.2 MySql JDBC Wrapper

This package contains the JDBC Wrapper for MySql for SIARD 2.2.

JdbcMySql 2.1 has been built and tested with JAVA JDK 1.8, 9, and 10.

## Getting started (for developers)

For building the binaries, Java JDK (1.8 or higher) and Ant must
have been installed. A copy of build.properties.template must be called
build.properties. In it using a text editor the local values must be
entered as directed by the comments.

JdbcMySql 2.1 has been built and tested with JAVA JDK 1.8, 9, and 10.

Run a MySQL 5 Database with: 

```shell
docker-compose up -d
```

### Build application artifacts

Run tests and build the package

```shell
./gradlew build
```

## Versioning, tags and releases

Versions and tags are managed with the Axion Release Plugin for Gradle (https://github.com/allegro/axion-release-plugin)

Short overview:

```shell
./gradlew currentVersion # show the current version

./gradlew release        # creates a new release adds a tag and pushes it to remote.
```

You may use an IDE of your choice for development (tested with intellij)

## Documentation

[./doc/manual/user/index.html](./doc/manual/user/index.html) contains the manual for using the binaries.
[./doc/manual/developer/index.html](./doc/manual/user/index.html) is the manual for developers wishing
build the binaries or work on the code.  

