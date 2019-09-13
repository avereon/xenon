#!/bin/bash

export DISPLAY=:99
Xvfb ${DISPLAY} -screen 0 1920x1080x24 -nolisten unix &
mvn deploy -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml
