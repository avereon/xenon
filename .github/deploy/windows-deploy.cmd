@echo off
setlocal enabledelayedexpansion

ver

set "USERHOME=."
set "SSHHOME=!HOMEPATH!\AppData\Roaming\_ssh"

rmdir /S /Q "!SSHHOME!"
mkdir "!SSHHOME!"
echo avereon.com,159.65.110.114 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw= >> !SSHHOME!\known_hosts
echo !TRAVIS_SSH_PUB! > !SSHHOME!\id_rsa.pub
for /f "delims=" %%g in ("!TRAVIS_SSH_KEY!") do echo %%g >> "!SSHHOME!\id_rsa"

REM dir "!SSHHOME!"
sha1sum "!SSHHOME!\id_rsa"
sha1sum "!SSHHOME!\id_rsa.pub"
sha1sum "!SSHHOME!\known_hosts"

REM scp -i "!SSHHOME!\id_rsa" "!SSHHOME!\id_rsa" travis@avereon.com:~/prvkey.txt
scp -v "!SSHHOME!\id_rsa.pub" travis@avereon.com:~/pubkey.txt
