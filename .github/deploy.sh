#!/bin/bash

#RELEASE github.ref [refs/heads/master, refs/heads/github-actions]
#OS matrix.os [ubuntu-latest, macOS-latest, windows-latest ]

echo "${GITHUB_REF}"
echo "${MATRIX_OS}"

#mkdir ${HOME}/.ssh
#echo "${TRAVIS_SSH_KEY}" > ${HOME}/.ssh/id_rsa
#echo "${TRAVIS_SSH_PUB}" > ${HOME}/.ssh/id_rsa.pub
#echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> ${HOME}/.ssh/known_hosts
#chmod 600 ${HOME}/.ssh/id_rsa
#chmod 600 ${HOME}/.ssh/id_rsa.pub
#chmod 600 ${HOME}/.ssh/known_hosts
#
#scp -B target/*install.jar travis@avereon.com:/opt/avn/store/latest/xenon/linux
#scp -B target/*product.jar travis@avereon.com:/opt/avn/store/latest/xenon/linux
#scp -B target/*product.zip travis@avereon.com:/opt/avn/store/latest/xenon/linux
#scp -B target/main/java/META-INF/*.card travis@avereon.com:/opt/avn/store/latest/xenon/linux
