@echo off
echo Dang khoi chay ung dung Quan ly Cua hang Dien thoai Di dong...
cd /d "%~dp0"
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=C:\Program Files\Java\jdk-17\bin;C:\Program Files\Apache NetBeans\java\maven\bin;C:\Program Files\MySQL\MySQL Server 8.4\bin;%PATH%
"C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" exec:java
pause
