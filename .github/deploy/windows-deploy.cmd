@echo off
setlocal enabledelayedexpansion

set "RELEASE=latest"
set "PRODUCT=xenon"
set "PLATFORM=windows"
set "USERHOME=!HOMEDRIVE!!HOMEPATH!"
set "SSHHOME=!USERHOME!\.ssh"

rmdir /S /Q "!SSHHOME!"
mkdir "!SSHHOME!"
echo avereon.com,159.65.110.114 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw= >> !SSHHOME!\known_hosts
echo.>> !SSHHOME!\known_hosts
echo !TRAVIS_SSH_PUB! > !SSHHOME!\id_rsa.pub
for /f "delims=" %%g in ("!TRAVIS_SSH_KEY!") do echo %%g >> "!SSHHOME!\id_rsa"

dir "!SSHHOME!"
sha1sum "!SSHHOME!\id_rsa"
sha1sum "!SSHHOME!\id_rsa.pub"
sha1sum "!SSHHOME!\known_hosts"

scp -B target/install.jar travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
scp -B target/product.jar travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
scp -B target/main/java/META-INF/product.card travis@avereon.com:/opt/avn/store/!RELEASE!/!PRODUCT!/!PLATFORM!
