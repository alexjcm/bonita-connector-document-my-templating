# bonita-connector-document-my-templating

[![License: GPL v2](https://img.shields.io/badge/License-GPL%20v2-yellow.svg)](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html)

Insert document properties inside .docx template

## Prerequisites

- Java 11
- Maven 3

## Bonita compatibility

Is compatible with Bonita version 2021.2 and above.

## Build

__Clone__ or __fork__ this repository, then at the root of the project run:

`./mvnw`

## Install

Starting with Bonita 2021.2, the following command must be executed to install the .jar locally and then import it from Bonita Studio.

`mvn clean install`

## Release

In order to create a new release push a `release-<version>` branch with the desired version in pom.xml.
Update the `master` with the next SNAPSHOT version.


## How to design report

### Using Word (docx)

* Insertion > QuickPart > Field...
* Select FusionField and use a template (see [Velocity templating language](http://velocity.apache.org/)) as **field name** (eg: ${name}, ${user.Name}...etc)
* Click OK

### Using LibreOffice (odt)

* Insert > Fields > More fields...
* Go to Variables tab, select UserField and use a template (see [Velocity templating language](http://velocity.apache.org/)) as **value** (eg: ${name}, ${user.Name}...etc)
* Choose Text format
* Click Insert

## Contributing

We would love you to contribute, pull requests are welcome! Please see the [CONTRIBUTING.md](CONTRIBUTING.md) for more information.

## License

The sources and documentation in this project are released under the [GPLv2 License](LICENSE)
