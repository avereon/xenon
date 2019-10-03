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

gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output .github/avereon.keystore .github/avereon.keystore.gpg

rm -rf target/jlink
mvn verify -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml

echo "Build date=$(date)"
echo "[github.ref]=${GITHUB_REF}"
echo "Deploy path=/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM"

mkdir ${HOME}/.ssh
#echo "${TRAVIS_SSH_KEY}" > ${HOME}/.ssh/id_rsa
#echo "${TRAVIS_SSH_PUB}" > ${HOME}/.ssh/id_rsa.pub
#echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> ${HOME}/.ssh/known_hosts

gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa .github/id_rsa.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa.pub .github/id_rsa.pub.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/known_hosts .github/known_hosts.gpg

chmod 600 ${HOME}/.ssh/id_rsa
chmod 600 ${HOME}/.ssh/id_rsa.pub
chmod 600 ${HOME}/.ssh/known_hosts

scp -B target/*install.jar travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM
scp -B target/*product.jar travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM
scp -B target/main/java/META-INF/*.card travis@avereon.com:/opt/avn/store/$RELEASE/$PRODUCT/$PLATFORM
