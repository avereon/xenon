#!/bin/bash

mkdir "${HOME}/.ssh"
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output .github/avereon.keystore .github/avereon.keystore.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa .github/id_rsa.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/id_rsa.pub .github/id_rsa.pub.gpg
gpg --quiet --batch --yes --decrypt --passphrase=$AVN_GPG_PASSWORD --output $HOME/.ssh/known_hosts .github/known_hosts.gpg
chmod 600 "${HOME}/.ssh/id_rsa"
chmod 600 "${HOME}/.ssh/id_rsa.pub"
chmod 600 "${HOME}/.ssh/known_hosts"
