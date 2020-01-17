#!/bin/bash

mkdir "${HOME}/.ssh"
gpg --quiet --batch --yes --decrypt --passphrase=${AVN_GPG_PASSWORD} --output .github/avereon.keystore .github/avereon.keystore.gpg
gpg --quiet --batch --yes --decrypt --passphrase=${AVN_GPG_PASSWORD} --output $HOME/.ssh/id_rsa .github/id_rsa.gpg
gpg --quiet --batch --yes --decrypt --passphrase=${AVN_GPG_PASSWORD} --output $HOME/.ssh/id_rsa.pub .github/id_rsa.pub.gpg
gpg --quiet --batch --yes --decrypt --passphrase=${AVN_GPG_PASSWORD} --output $HOME/.ssh/known_hosts .github/known_hosts.gpg
chmod 600 "${HOME}/.ssh/id_rsa"
chmod 600 "${HOME}/.ssh/id_rsa.pub"
chmod 600 "${HOME}/.ssh/known_hosts"

#AVN_RELEASE is derived from github.ref, example values: refs/heads/master, refs/heads/stable
case "${GITHUB_REF}" in
  "refs/heads/master") AVN_RELEASE="latest" ;;
  "refs/heads/stable") AVN_RELEASE="stable" ;;
esac
export PRODUCT_DEPLOY_PATH=/opt/avn/store/${AVN_RELEASE}/${AVN_PRODUCT}/${AVN_PLATFORM}

echo "Build date=$(date)"
echo "GITHUB_REF=${GITHUB_REF}"
echo "PRODUCT_DEPLOY_PATH=${PRODUCT_DEPLOY_PATH}"

echo "::set-env name=JAVADOC_DEPLOY_PATH::/opt/avn/store/latest/${AVN_PRODUCT}"
echo "::set-env name=JAVADOC_TARGET_PATH::/opt/avn/web/product/${AVN_PRODUCT}/javadoc"
echo "::set-env name=PRODUCT_DEPLOY_PATH::${PRODUCT_DEPLOY_PATH}"
