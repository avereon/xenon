#!/bin/bash

echo "build date=$(date)"
echo "[github.ref]=${GITHUB_REF}"
echo "[matrix.os]=${MATRIX_OS}"

mvn test -B -U -V --settings .github/settings.xml --file pom.xml
