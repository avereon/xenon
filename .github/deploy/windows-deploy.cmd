@echo off

set USERHOME="%HOMEDRIVE%%HOMEPATH%"

mkdir "%USERHOME%\.ssh"
@echo 'avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw=' >> %USERHOME%\.ssh\known_hosts
@echo "%TRAVIS_SSH_PUB%" > %USERHOME%\.ssh\id_rsa.pub
for /f "delims=" %%g in ("%TRAVIS_SSH_KEY%") do @echo %%g >> %USERHOME%\.ssh\id_rsa

dir "%USERHOME%\.ssh"
type %USERHOME%\.ssh\id_rsa.pub

REM Use Maven to verify the build, but do not deploy it to the repository
REM mvn verify -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml

@echo "Hello Mark" > hello.txt
scp hello.txt travis@avereon.com:~
