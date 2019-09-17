@echo off
setlocal enabledelayedexpansion

set USERHOME="!HOMEDRIVE!!HOMEPATH!"

rmdir /S /Q .ssh
mkdir "!USERHOME!\.ssh"
echo avereon.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBAX0k5tSvrXVpKl7HNPIPglp6Kyj0Ypty6M3hgR783ViTzhRnojEZvdCXuYiGSVKEzZWr9oYQnLr03qjU/t0SNw= >> !USERHOME!\.ssh\known_hosts
echo !TRAVIS_SSH_PUB! > !USERHOME!\.ssh\id_rsa.pub
for /f "delims=" %%g in ("!TRAVIS_SSH_KEY!") do echo %%g >> !USERHOME!\.ssh\id_rsa

dir .ssh

scp .ssh/id_rsa travis@avereon.com:~/prvkey.txt
scp .ssh/id_rsa.pub travis@avereon.com:~/pubkey.txt
