#!/usr/bin/env bash

mvn clean compile package

if [ ! -d $1 ]
then
    mkdir $1
fi

cp target/client-1.0-SNAPSHOT-jar-with-dependencies.jar "$1"/client.jar
cp src/main/python/nfov.py "$1"/nfov.py
cp src/main/scripts/backup.sh "$1"/backup.sh
cp src/main/scripts/restore.sh "$1"/restore.sh
cp src/main/scripts/upload.sh "$1"/upload.sh
cp src/main/scripts/sql.sh "$1"/sql.sh
cp src/main/scripts/script.sql "$1"/script.sql