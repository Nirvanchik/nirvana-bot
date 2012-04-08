#set CLASSPATH=..\lib\commons-lang3-3.0.1.jar;..\lib\log4j-1.2.16.jar;..\bin;
#echo %CLASSPATH%
#echo hello  
##sdfdsf

java -classpath .\bin;.\lib\commons-lang3-3.0.1.jar;.\lib\log4j-1.2.16.jar org.wikipedia.nirvana.nirvanabot.NirvanaBot config_evaluate.xml
