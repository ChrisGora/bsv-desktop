#!/usr/bin/env bash

a=$HOME
b="/"
c="rtree.tree"
path=$a$b$1$b$c

if [ ! -d $1 ]
then
    mkdir $1
fi

d="rtree_backup.tree"
rtreeBackupPath=$1${b}${d}
e="backup.sql"
sqlBackupPath=$1${b}${e}

echo ${path}
echo ${rtreeBackupPath}
cp ${path} ${rtreeBackupPath}
mysqldump -N -u root -pCy3M22Yar2UJ --databases bristol_streetview_schema > ${sqlBackupPath}