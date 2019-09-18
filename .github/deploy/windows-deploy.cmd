@echo off
setlocal enabledelayedexpansion

ver

set "USERHOME=."

rmdir /S /Q "!USERHOME!\.ssh"
mkdir "!USERHOME!\.ssh"
echo avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw= >> !USERHOME!\.ssh\known_hosts
echo !TRAVIS_SSH_PUB! > !USERHOME!\.ssh\id_rsa.pub
for /f "delims=" %%g in ("!TRAVIS_SSH_KEY!") do echo %%g >> "!USERHOME!\.ssh\id_rsa"

dir "!USERHOME!\.ssh"
md5sum "!USERHOME!\.ssh\*sc "

scp "!USERHOME!\.ssh\id_rsa" travis@avereon.com:~/prvkey.txt
scp "!USERHOME!\.ssh\id_rsa.pub" travis@avereon.com:~/pubkey.txt
