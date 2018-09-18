#!/usr/bin/env bash

mvn clean compile package

if [ ! -d $1 ]
then
    mkdir $1
fi

cp target/client-1.0-SNAPSHOT-jar-with-dependencies.jar "$1"/client.jar