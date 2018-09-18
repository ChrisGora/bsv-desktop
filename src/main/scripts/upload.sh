#!/usr/bin/env bash

cd /run/user/$UID/gvfs/mtp*/
cd Internal\ storage/Ricoh/
STR="$(pwd)"
STR="$(echo -e "\x22$STR\x22")"
echo $STR

if [ -a $HOME/client/log.txt ]
then
    rm $HOME/client/log.txt
fi

java -jar $HOME/client/client.jar -b=bsv -vepf=$HOME/client/log.txt -r="$1" -gu="$STR"