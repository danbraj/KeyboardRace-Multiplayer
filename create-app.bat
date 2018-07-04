@echo off

echo c ~ create executable client app
echo s ~ create executable server app
echo x ~ exit

choice /c csx /m "What can I do for you? > "
if errorlevel 3 goto end
if errorlevel 2 goto serverApp
if errorlevel 1 goto clientApp

:clientApp
javac -encoding utf8 ClientApp.java
echo Main-Class: ClientApp > manifest_for_client.txt
jar cvfm ClientApp.jar manifest_for_client.txt *.class
del manifest_for_client.txt *.class
goto end

:serverApp
javac -encoding utf8 ServerApp.java
echo Main-Class: ServerApp > manifest_for_server.txt
jar cvfm ServerApp.jar manifest_for_server.txt *.class
del manifest_for_server.txt *.class
::goto end

:end
echo Bye!
pause