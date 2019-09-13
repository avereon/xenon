#!/bin/bash

if [ "${MATRIX_OS}" == "ubuntu-latest" ]; then
  export DISPLAY=:99
  Xvfb ${DISPLAY} -screen 0 1920x1080x24 -nolisten unix &
fi

mvn deploy -B -U -V --settings .github/settings.xml --file pom.xml
