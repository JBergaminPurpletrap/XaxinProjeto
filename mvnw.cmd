@REM ----------------------------------------------------------------------------
@REM Maven Wrapper script for Windows
@REM ----------------------------------------------------------------------------
@REM This script downloads and runs Maven to build the project
@REM ----------------------------------------------------------------------------
@REM Determine the Java command

@if not "%JAVA_HOME%" == "" goto gotJavaHome
echo Error: JAVA_HOME not set. Please set JAVA_HOME to your JDK installation.
echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-17
exit /b 1
:gotJavaHome

@set "MAVEN_REPO=%USERPROFILE%\.m2\repository"
@set "MAVEN_VERSION=3.9.6"
@set "MAVEN_URL=https://dlcdn.apache.org/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip"
@set "MAVEN_ZIP=%TEMP%\apache-maven-%MAVEN_VERSION%-bin.zip"
@set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\apache-maven-%MAVEN_VERSION%"

@if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runMaven

echo Downloading Maven %MAVEN_VERSION%...
@powershell -Command "& { Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%' }"
if errorlevel 1 (
    echo Failed to download Maven from %MAVEN_URL%
    echo Will try system 'mvn' instead.
    goto runSystemMaven
)

@powershell -Command "& { Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%USERPROFILE%\.m2\wrapper' -Force }"
if errorlevel 1 (
    echo Failed to extract Maven
    echo Will try system 'mvn' instead.
    goto runSystemMaven
)

del "%MAVEN_ZIP%"

:runMaven
if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    "%MAVEN_HOME%\bin\mvn.cmd" %*
) else (
    goto runSystemMaven
)
goto :eof

:runSystemMaven
echo Running system 'mvn' as fallback
mvn %*
goto :eof