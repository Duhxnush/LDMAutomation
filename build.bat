@ECHO OFF
call :run_maven_commands

REM Check if Maven build was successful
if %errorlevel% neq 0 (
    echo Maven build failed. Exiting...
    exit /b %errorlevel%
)

:choice
set /P c=Do you want to execute the testcase with local Code editor Jar instead of Active Jar in Custom Code editor with Yes and No options: 
if /I "%c%" EQU "Yes" goto :somewhere
if /I "%c%" EQU "No" goto :somewhere_else
goto :choice

:run_maven_commands
cmd /c "mvn install:install-file -Dfile=%appdata%\SQA-Agent\codeEditorProjects\libs\com.simplifyQA.Agent.jar -DgroupId=simplifyagent -DartifactId=simplifyagent -Dversion=1.0 -Dpackaging=jar & mvn install -X"
goto :eof

:somewhere_else
mkdir config
echo Config: cloud> config\file.config
exit


:somewhere
mkdir config
echo Config: local> config\file.config
exit

