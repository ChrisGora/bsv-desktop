#!/bin/bash

mysql -N -u java-db-client -pRe278nErRowD  < script.sql > out.txt
java -jar $HOME/client/client.jar -b=$1 -vepf=$HOME/client/log.txt -s @out.txt