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

rmdir /S /Q "!SSHHOME!"
mkdir "!SSHHOME!"
echo avereon.com,159.65.110.114 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw= >> !SSHHOME!\known_hosts
echo.>> !SSHHOME!\known_hosts
echo !TRAVIS_SSH_PUB! > !SSHHOME!\id_rsa.pub
for /f "delims=" %%g in ("!TRAVIS_SSH_KEY!") do echo %%g >> "!SSHHOME!\id_rsa"

cmd /c gpg --quiet --batch --yes --decrypt --passphrase=!AVN_GPG_PASSWORD! --output .github\avereon.keystore .github\avereon.keystore.gpg

dir "!SSHHOME!"
sha1sum "!SSHHOME!\id_rsa"
sha1sum "!SSHHOME!\id_rsa.pub"
sha1sum "!SSHHOME!\known_hosts"

# Test sending before building
scp -B source/main/resources/ascii-art-title.txt travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!

rmdir /S /Q target\jlink
cmd /c mvn verify -B -U -V -P testui,platform-specific-assemblies --settings .github/settings.xml --file pom.xml

scp -B target/install.jar travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
scp -B target/product.jar travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
scp -B target/main/java/META-INF/product.card travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
