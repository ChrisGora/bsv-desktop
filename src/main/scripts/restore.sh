#!/usr/bin/env bash

a=$HOME
b="/"
c="rtree.tree"
path=$a$b$1$b$c
#echo $path

d="rtree_backup.tree"
rtreeBackupPath=$1${b}${d}
e="backup.sql"
sqlBackupPath=$1${b}${e}

cp ${rtreeBackupPath} ${path}
mysql -N -u root -pCy3M22Yar2UJ  < ${sqlBackupPath}
