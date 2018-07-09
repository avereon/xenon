#!/bin/bash

WORK_FOLDER=target/modpatch
MODULE_INFO_FOLDER=$WORK_FOLDER

MODULE_PATH=target/pack/program/mod

V1=target/pack/program/mod/reactfx.jar

JAR_NAME=$(basename "$V1")
MOD_NAME="${JAR_NAME%.*}"

echo "Jar name: $JAR_NAME"
echo "Mod name: $MOD_NAME"

# Need to move module to fix out of the module path
mkdir -p "$WORK_FOLDER"
mv "$V1" "$WORK_FOLDER"

$JAVA_HOME/bin/jdeps --module-path "$MODULE_PATH" --generate-module-info "$MODULE_INFO_FOLDER" "$WORK_FOLDER/$JAR_NAME"
$JAVA_HOME/bin/javac -p $MODULE_PATH --patch-module $MOD_NAME="$WORK_FOLDER/$JAR_NAME" "$WORK_FOLDER/$MOD_NAME/module-info.java"
$JAVA_HOME/bin/jar uf "$WORK_FOLDER/$JAR_NAME" -C "$WORK_FOLDER/$MOD_NAME" module-info.class

mv "$WORK_FOLDER/$JAR_NAME" "$MODULE_PATH"
