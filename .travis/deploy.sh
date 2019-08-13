#!/bin/bash

RELEASE=latest
PRODUCT=xenon
PLATFORM=linux

# Maven deploy
mvn --settings .travis/settings.xml -DskipTests=true -Dmaven.javadoc.skip=true -B -U -V -P platform-agnostic-assemblies deploy

# Avereon deploy
echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> $HOME/.ssh/known_hosts
openssl aes-256-cbc -K $encrypted_27c8e7206800_key -iv $encrypted_27c8e7206800_iv -in .travis/id_rsa.enc -out $HOME/.ssh/id_rsa -d
chmod 600 "$HOME"/.ssh/*
scp -B target/product.jar travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM/product.jar && echo "Deployed /opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM/product.jar"
scp -B target/install.jar travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM/install.jar && echo "Deployed /opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM/install.jar"
scp -B target/main/java/META-INF/product.card travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM/product.card && echo "Deployed /opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM/product.card"
# TODO Deploy and image
