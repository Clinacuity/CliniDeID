@setlocal enableextensions enabledelayedexpansion
@echo off
IF EXIST "openJdk17.0.1\bin\" (
    SET JH=openJdk17.0.1\bin
) ELSE IF EXIST "jdk-17.0.1\bin\" (
    SET JH=jdk-17.0.1\bin
) ELSE (
    echo "Can't find Java"
    GOTO END
)
%JH%\java -Xmx28g -cp CliniDeIDComplete.jar com.clinacuity.deid.mains.DeidPipeline %*

set JH=

:END
