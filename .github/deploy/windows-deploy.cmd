@echo off
setlocal enabledelayedexpansion

set "RELEASE=latest"
set "PRODUCT=xenon"
set "PLATFORM=windows"
set "USERHOME=!HOMEDRIVE!!HOMEPATH!"
set "SSHHOME=!USERHOME!\.ssh"

IF "!GITHUB_REF!"=="refs/heads/stable" set "RELEASE=stable"
for /f "tokens=* USEBACKQ" %%g IN (`date /T`) DO SET DATEVAL=%%g
for /f "tokens=* USEBACKQ" %%g IN (`time /T`) DO SET TIMEVAL=%%g

echo "Build timestamp=!DATEVAL! !TIMEVAL!"
echo "[github.ref]=!GITHUB_REF!"
echo "Deploy path=/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!"

cmd /c gpg --quiet --batch --yes --decrypt --passphrase=!AVN_GPG_PASSWORD! --output .github\avereon.keystore .github\avereon.keystore.gpg
rmdir /S /Q target\jlink && cmd /c mvn verify -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml
if %ERRORLEVEL% GEQ 1 exit 1

rmdir /S /Q "!SSHHOME!"
mkdir "!SSHHOME!"
cmd /c gpg --quiet --batch --yes --decrypt --passphrase=!AVN_GPG_PASSWORD! --output "!SSHHOME!\id_rsa" .github\id_rsa.gpg
cmd /c gpg --quiet --batch --yes --decrypt --passphrase=!AVN_GPG_PASSWORD! --output "!SSHHOME!\id_rsa.pub" .github\id_rsa.pub.gpg
cmd /c gpg --quiet --batch --yes --decrypt --passphrase=!AVN_GPG_PASSWORD! --output "!SSHHOME!\known_hosts" .github\known_hosts.gpg

dir "!SSHHOME!"
sha1sum "!SSHHOME!\id_rsa"
sha1sum "!SSHHOME!\id_rsa.pub"
sha1sum "!SSHHOME!\known_hosts"

scp -B target/install.jar travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
if %ERRORLEVEL% GEQ 1 exit 1
scp -B target/product.jar travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
if %ERRORLEVEL% GEQ 1 exit 1
scp -B target/main/java/META-INF/product.card travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
if %ERRORLEVEL% GEQ 1 exit 1
