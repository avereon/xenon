#!/bin/bash

RELEASE='latest'
PRODUCT='xenon'
DEPLOY_PATH="/opt/avn/store/$RELEASE/$PRODUCT"

#RELEASE github.ref [refs/heads/master, refs/heads/stable]
#case "${GITHUB_REF}" in
#  "refs/heads/master") RELEASE="latest" ;;
#  "refs/heads/stable") RELEASE="stable" ;;
#esac

echo "Build date=$(date)"
echo "[github.ref]=${GITHUB_REF}"
echo "Deploy path=$DEPLOY_PATH"

mkdir "${HOME}/.ssh"
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa .github/id_rsa.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa.pub .github/id_rsa.pub.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/known_hosts .github/known_hosts.gpg

chmod 600 "${HOME}/.ssh/id_rsa"
chmod 600 "${HOME}/.ssh/id_rsa.pub"
chmod 600 "${HOME}/.ssh/known_hosts"

#ls -al "$HOME/.ssh"
#sha1sum "$HOME/.ssh/id_rsa"
#sha1sum "$HOME/.ssh/id_rsa.pub"
#sha1sum "$HOME/.ssh/known_hosts"

JAVADOC="/opt/avn/web/product/xenon/javadoc"
scp -B target/xenon-*-javadoc.jar travis@avereon.com:$DEPLOY_PATH/javadoc.jar 2>&1
if [ $? -ne 0 ]; then exit 1; fi
ssh -t travis@avereon.com "mkdir -p $JAVADOC;rm -rf $JAVADOC/*;unzip -o $DEPLOY_PATH/javadoc.jar -d $JAVADOC"
if [ $? -ne 0 ]; then exit 1; fi
