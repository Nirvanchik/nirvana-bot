@if "%DEBUG%" == "" @echo off

set CMD_LINE_ARGS=%*

@if "%CMD_LINE_ARGS%" == "" goto invalidargs

echo java -jar .checkstyle\checkstyle-6.19-all.jar -c .checkstyle\checks.xml %CMD_LINE_ARGS%
java -jar .checkstyle\checkstyle-6.19-all.jar -c .checkstyle\checks.xml %CMD_LINE_ARGS%

if "%ERRORLEVEL%"=="0" goto fail
exit /b 0


:invalidargs
echo No files specified. Please call this script in the next format:
echo check-new-source.cmd [PATH_TO_YOU_NEW_JAVA_SOURCES_SEPARATED_BY_SPACE]
exit /b 1

:fail
echo "checkstyle found some problems with your sources."
exit /b 1

