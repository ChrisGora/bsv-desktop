#!/usr/bin/env bash


./backup.sh $1
cd /run/user/$UID/gvfs/mtp*/
cd Internal\ storage/Ricoh/
STR="$(pwd)"
STR="$(echo -e "\x22$STR\x22")"
echo $STR

if [ -a log.txt ]
then
    rm log.txt
fi

java -jar client.jar -b=$1 -vepf=log.txt -r="$2" -gu="$STR"