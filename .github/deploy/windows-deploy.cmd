@echo off
setlocal enabledelayedexpansion

ver

set "USERHOME=."

rmdir /S /Q "!USERHOME!\.ssh"
mkdir "!USERHOME!\.ssh"
echo avereon.com,159.65.110.114 ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw= >> !USERHOME!\.ssh\known_hosts
echo !TRAVIS_SSH_PUB! > !USERHOME!\.ssh\id_rsa.pub
for /f "delims=" %%g in ("!TRAVIS_SSH_KEY!") do echo %%g >> "!USERHOME!\.ssh\id_rsa"

dir "!USERHOME!\.ssh"
sha1sum "!USERHOME!\.ssh\id_rsa"
sha1sum "!USERHOME!\.ssh\id_rsa.pub"
type "!USERHOME!\.ssh\known_hosts"

scp -i "!USERHOME!\.ssh\id_rsa" "!USERHOME!\.ssh\id_rsa" travis@avereon.com:~/prvkey.txt
scp -i "!USERHOME!\.ssh\id_rsa" "!USERHOME!\.ssh\id_rsa.pub" travis@avereon.com:~/pubkey.txt
