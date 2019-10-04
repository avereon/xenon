#!/bin/bash

RELEASE="latest"
PRODUCT='xenon'
PLATFORM='linux'

#RELEASE github.ref [refs/heads/master, refs/heads/stable]
case "${GITHUB_REF}" in
  "refs/heads/master") RELEASE="latest" ;;
  "refs/heads/stable") RELEASE="stable" ;;
esac

if [ "${PLATFORM}" == "linux" ]; then
  export DISPLAY=:99
  Xvfb ${DISPLAY} -screen 0 1920x1080x24 -nolisten unix &
fi

echo "Build date=$(date)"
echo "[github.ref]=${GITHUB_REF}"
echo "Deploy path=/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM"

mkdir ${HOME}/.ssh
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa .github/id_rsa.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa.pub .github/id_rsa.pub.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/known_hosts .github/known_hosts.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output .github/avereon.keystore .github/avereon.keystore.gpg
#echo "${TRAVIS_SSH_KEY}" > ${HOME}/.ssh/id_rsa
#echo "${TRAVIS_SSH_PUB}" > ${HOME}/.ssh/id_rsa.pub
#echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> ${HOME}/.ssh/known_hosts

chmod 600 ${HOME}/.ssh/id_rsa
chmod 600 ${HOME}/.ssh/id_rsa.pub
chmod 600 ${HOME}/.ssh/known_hosts

ls -al $HOME/.ssh
sha1sum $HOME/.ssh/id_rsa
sha1sum $HOME/.ssh/id_rsa.pub
sha1sum $HOME/.ssh/known_hosts

# Test sending before building
scp -B source/main/resources/ascii-art-title.txt travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM 2>&1
if [ $? -ne 0 ]; then exit 1; fi

rm -rf target/jlink
mvn verify -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml
if [ $? -ne 0 ]; then exit 1; fi

scp -B target/*install.jar travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM 2>&1
if [ $? -ne 0 ]; then exit 1; fi
scp -B target/*product.jar travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM 2>&1
if [ $? -ne 0 ]; then exit 1; fi
scp -Bv target/main/java/META-INF/*.card travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM 2>&1
if [ $? -ne 0 ]; then exit 1; fi
