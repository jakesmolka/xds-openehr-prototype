### Origin
Forked from [IPF XDS tutorial](https://github.com/oehf/ipf/tree/d4d8807020e5d168b5905c7b7f953dfcd8ac8062/tutorials/xds).

### Purpose

Prototype to test expansion of IPF XDS to be wrapped by [openEHR REST API](https://github.com/openEHR/specifications-ITS/tree/8b4d9c0f1772e268cf0ec0516eafdd4a0e7408a9).

### Steps 

See [XDS tutorial](https://oehf.github.io/ipf/ipf-tutorials-xds/index.html) for more information.

Minimal example to build and run:

1. run ``mvn clean install assembly:assembly``
2. unzip ``target/ipf-tutorials-xds-$VERSION-bin.zip``
3. run extracted ``startup.sh`` or ``startup.bat`` file