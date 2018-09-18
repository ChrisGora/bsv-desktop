#!/bin/bash

mysql -N -u java-db-client -p  < script.sql > out.txt
java -jar $HOME/client/client.jar -b=bsv -vepf=$HOME/client/log.txt -s @out.txt