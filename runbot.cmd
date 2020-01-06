@if "%DEBUG%" == "" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

if "%BOT_APP%" == "" goto setapp

if "%BOT_APP%" == "nirvana-bot" (
    set MAIN_CLASS=org.wikipedia.nirvana.nirvanabot.NirvanaBot
)
if "%BOT_APP%" == "statistics-bot" (
    set MAIN_CLASS=org.wikipedia.nirvana.statistics.StatisticsBot
)
if "%BOT_APP%" == "archive-bot" (
    set MAIN_CLASS=org.wikipedia.nirvana.archive.NirvanaArchiveBot
)
if "%BOT_APP%" == "clean-archive-bot" (
    set MAIN_CLASS=org.wikipedia.nirvana.cleanarchive.CleanArchiveBot
)
if "%BOT_APP%" == "list-tools" (
    set MAIN_CLASS=org.wikipedia.nirvana.ListTools
)
if "%BOT_APP%" == "archive-tools" (
    set MAIN_CLASS=org.wikipedia.nirvana.archive.ArchiveTools
)

if "%MAIN_CLASS%" == "" goto unexpectedbotapp

if exist "target\nirvana-bot-full.jar" goto mavendev
if exist "build\install\NirvanaBot" goto gradledev
if exist "bin\%BOT_APP%" goto gradledistrib

echo Sorry! No bot distribution found
goto fail

:mavendev
echo Launch %BOT_APP% from Maven target dir.
if "%BOT_APP%" == "nirvana-bot" (
    java -jar target/nirvana-bot-full.jar %*
    goto mainEnd
)
java -cp target/nirvana-bot-full.jar %MAIN_CLASS% %*
goto mainEnd

:unexpectedbotapp
echo Unexpected BOT_APP value: %BOT_APP%
goto allowedbotapp

:gradledev
echo Launch %BOT_APP% from Gradle build dir.
if exist "build\install\NirvanaBot\bin\%BOT_APP%" (
    "build\install\NirvanaBot\bin\%BOT_APP%" %*
	goto mainEnd
)
java -classpath ^.\build\install\NirvanaBot\lib\* %MAIN_CLASS% %*
goto mainEnd

:gradledistrib
echo Launch %BOT_APP% from Gradle distribution dir.
"bin\%BOT_APP%" %*
goto mainEnd

:: --------------- ERROR HANDLING ---------------
:setapp
echo.
echo BOT_APP variable not set. Please set BOT_APP to required script name.
:allowedbotapp
echo Allowed values for BOT_APP: nirvana-bot, statistics-bot, archive-bot, clean-archive-bot.

:fail
exit /b 1

:: --------------- END ---------------
:mainEnd
if "%OS%"=="Windows_NT" endlocal
