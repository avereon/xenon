#!/bin/bash

# This script is to assist deployment of javafx artifacts to a maven repository

# It takes a bit of setup to make this script work. Each of the three OS folders
# must be unpacked and renamed to have the same root name with the OS identifier
# suffix. Example: If you chose to have the root name be javafx-sdk-11 then the
# folders would be javafx-sdk-11-linux, javafx-sdk-11-mac and javafx-sdk-11-win
# respectively for Linux, MacOS and Windows.

# Example: source/main/bin/deploy-javafx.sh ~/Downloads/javafx-sdk-11 11-ea-18

REPO_URL='https://code.xeomar.com/repo/thirdparty/'

FOLDER="$1"
VERSION="$2"

deploy() {
    PLACEHOLDER="/tmp/$1.jar"
    FILES="/$FOLDER-linux/lib/$1.jar,/$FOLDER-mac/lib/$1.jar,/$FOLDER-win/lib/$1.jar"
    CLASSIFIERS="linux,mac,win"
    TYPES="jar,jar,jar"

    rm -f "$PLACEHOLDER"
    touch "$PLACEHOLDER"

    mvn deploy:deploy-file -Durl="$REPO_URL" -Dfile="$PLACEHOLDER" -Dfiles="$FILES" -Dclassifiers="$CLASSIFIERS" -Dtypes="$TYPES" -DrepositoryId="xeo" -DgroupId="org.javafx" -DartifactId="$1" -Dversion="$VERSION"
}

deploy javafx.base
deploy javafx.controls
deploy javafx.fxml
deploy javafx.graphics
deploy javafx.media
deploy javafx.swing
deploy javafx.web
