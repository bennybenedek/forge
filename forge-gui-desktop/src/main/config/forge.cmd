@echo off

pushd %~dp0

:: Check for bundled JRE first
if exist "%~dp0jre17\bin\java.exe" (
  set "JAVA_CMD=%~dp0jre17\bin\java.exe"
  goto :run
)

:: Fall back to system Java
java -version 1>nul 2>nul || (
   echo no java installed
   popd
   exit /b 2
)
for /f tokens^=2^ delims^=.-_^+^" %%j in ('java -fullversion 2^>^&1') do set "jver=%%j"

if %jver% LEQ 16 (
   echo unsupported java
   popd
   exit /b 2
)

set "JAVA_CMD=java"

:run
"%JAVA_CMD%" $mandatory.java.args$ -jar $project.build.finalName$
popd
exit /b 0
