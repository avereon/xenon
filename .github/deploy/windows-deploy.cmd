@echo off

set HOME="%HOMEDRIVE%%HOMEPATH%"

mkdir "%HOME%\.ssh"
@echo "" > %HOME%\.ssh\id_rsa
for %f in ("%TRAVIS_SSH_KEY%") do @echo %f >> %HOME%\.ssh\id_rsa
@echo "%TRAVIS_SSH_PUB%" > %HOME%\.ssh\id_rsa.pub
@echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> %HOME%\.ssh\known_hosts

dir "%HOME%\.ssh"

REM Use Maven to verify the build, but do not deploy it to the repository
REM mvn verify -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml

