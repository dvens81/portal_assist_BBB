@echo off
copy /Y webdriver\chromedriver-win webdriver\chromedriver
set JAVA_HOME=C:\Program Files\Java\jdk-11.0.7
SET PATH=%JAVA_HOME%\bin;%PATH%
java -jar bbb-tester-0.0.1-SNAPSHOT.jar
