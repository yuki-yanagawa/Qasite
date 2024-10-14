@echo off
if "%1" == "" (
	set hostname=localhost
) else (
	set hostname=%1
)
echo.
set /p yn_check="hostname is %hostname%. Is this OK?[y/n] "
echo.

if %yn_check%==n (
	echo Please setting retry. if you want to set another hostname, please set args. exampele:  createKey.bat 192.168.XX.XX
	goto End
)

where openssl > nul
if not %ERRORLEVEL% == 0 (
	echo Please install. openssl!!!
	goto End
)

echo +++++++++++ creatCAKey start ++++++++++++++++++++++++
echo.
openssl genrsa -out keydir\ca.key 2048
echo.
openssl req -new -key keydir\ca.key -subj "/C=JP/ST=Miyagi/O=TestOrg/CN=myca" -out keydir\ca.csr
echo.
echo subjectAltName=DNS:localhost, IP:127.0.0.1 > keydir\subjectnamesforca.txt
openssl x509 -days 365 -req -extfile keydir\subjectnamesforca.txt -signkey keydir\ca.key -in keydir\ca.csr -out keydir\ca.crt
echo.
echo +++++++++++ creatCAKey end ++++++++++++++++++++++++
echo.
echo.
echo.
echo +++++++++++ creatServerKey start ++++++++++++++++++++++++
echo.
openssl genrsa -out keydir\server.key 2048
echo.
openssl req -new -key keydir\server.key -subj "/C=JP/ST=Miyagi/O=QAStiteServer/CN=%hostname%" -out keydir\server.csr
echo.
if %hostname% == localhost (
	set altname=subjectAltName=DNS:localhost, IP:127.0.0.1
) else (
	set altname=subjectAltName=DNS:%hostname%, IP:%hostname%
)
echo %altname% > keydir\subjectnamesforserver.txt
openssl x509 -days 365 -req -CA keydir\ca.crt -CAkey keydir\ca.key -CAcreateserial -extfile keydir\subjectnamesforserver.txt -in keydir\server.csr -out keydir\server.crt
openssl pkcs12 -export -in keydir\server.crt -inkey keydir\server.key -out keydir\server.p12

echo +++++++++++ creatServerKey start ++++++++++++++++++++++++
echo.
echo.
echo.
echo serverkey is created.

:End
echo.
pause
