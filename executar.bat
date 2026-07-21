@echo off
setlocal

set "JAR_FILE=target\sistema-bilhetagem-vt-1.0.0-SNAPSHOT.jar"

if not exist "%JAR_FILE%" (
    echo [ERRO] JAR nao encontrado em "%JAR_FILE%".
    echo Gere o JAR primeiro com: mvn clean package
    pause
    exit /b 1
)

java -jar "%JAR_FILE%"

if errorlevel 1 (
    echo.
    echo [ERRO] O sistema encerrou com erro.
    pause
)

endlocal