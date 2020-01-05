@if "%DEBUG%" == "" @echo off

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

if "%BOT_APP%" == "" goto setapp

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
if "%BOT_APP%" == "statistics-bot" (
    java -cp target/nirvana-bot-full.jar org.wikipedia.nirvana.statistics.StatisticsBot %*
	goto mainEnd
)
if "%BOT_APP%" == "archive-bot" (
    java -cp target/nirvana-bot-full.jar org.wikipedia.nirvana.archive.NirvanaArchiveBot %*
	goto mainEnd
)
if "%BOT_APP%" == "clean-archive-bot" (
    java -cp target/nirvana-bot-full.jar org.wikipedia.nirvana.cleanarchive.CleanArchiveBot %*
	goto mainEnd
)
echo Unexpected BOT_APP value: %BOT_APP%
goto allowedbotapp

:gradledev
echo Launch %BOT_APP% from Gradle build dir.
"build\install\NirvanaBot\bin\%BOT_APP%" %*
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
