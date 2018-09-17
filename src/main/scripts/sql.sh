#!/bin/bash

mysql -N -u java-db-client -p  < script.sql > out.txt
java -jar target/client-1.0-SNAPSHOT-jar-with-dependencies.jar -ve -s bsv @out.txt