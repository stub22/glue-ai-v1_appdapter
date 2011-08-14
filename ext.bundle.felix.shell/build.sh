#!/bin/sh

mvn clean
mvn package
mvn -Prun-on-felix antrun:run