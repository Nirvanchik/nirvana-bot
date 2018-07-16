#!/bin/bash

echo java -jar .checkstyle/checkstyle-6.19-all.jar -c .checkstyle/checks.xml $1 $2 $3 $4 $5 $6 $7 $8 $9
java -jar .checkstyle/checkstyle-6.19-all.jar -c .checkstyle/checks.xml $1 $2 $3 $4 $5 $6 $7 $8 $9

if [ "$?" != "0" ]; then
  echo "checkstyle found some problems with your sources.";
  exit 1;
fi
