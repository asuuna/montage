@echo off
setlocal
set "APP_IMAGE=app-launcher\target\installer\Montage\Montage.exe"
set "RUNTIME_JAVA=app-launcher\target\installer\Montage\runtime\bin\java.exe"
set "FALLBACK_JAR=app-launcher\target\jpackage\app-launcher-0.1.0-SNAPSHOT.jar"
if exist "%APP_IMAGE%" (
    echo Lancement de Montage.exe...
    start "" "%APP_IMAGE%"
    goto :eof
)
if exist "%RUNTIME_JAVA%" if exist "%FALLBACK_JAR%" (
    echo Lancement via le runtime embarque...
    "%RUNTIME_JAVA%" -jar "%FALLBACK_JAR%"
    goto :eof
)
echo Impossible de trouver l'image jpackage ou le jar.
echo Lance d'abord ^".\mvnw -pl app-launcher -am -Pinstaller -DskipTests package^".
exit /b 1

