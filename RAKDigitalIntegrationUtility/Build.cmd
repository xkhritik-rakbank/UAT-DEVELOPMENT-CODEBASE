@REM Copyright (c) 2004 NEWGEN All Rights Reserved.

@REM *********************JAR BUILDING***************************************************************************
@REM Modify these variables to match your environment
	cls
	set JAVA_HOME="C:\Program Files\Java\jdk1.8.0_144"
	set MYCLASSPATH=bin
	set JARPATH=..
@REM ************************************************************************************************

 	cd %MYCLASSPATH%

@REM mqsocketserver jar
    %JAVA_HOME%\bin\jar -cvfm %JARPATH%\Rak_Digital_Integration_Utility.jar ..\MANIFEST.MF com\newgen\common\*.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\iRBL\AttachDocument\*.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\iRBL\BAISWICreate\*.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\iRBL\iRBLHoldInBAISProcess\*.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\iRBL\CIFUpdate\*.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\iRBL\CIFVerification\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\iRBL\FircoHold\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\iRBL\AOApprovalHold\*.class
    %JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\iRBL\SysCheckIntegration\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\DAO\AWB\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\DAO\Notify\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\DAO\Prime\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\DAO\common\*.class
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar com\newgen\main\*.class
	
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\common\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\iRBL\AttachDocument\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\iRBL\BAISWICreate\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\iRBL\iRBLHoldInBAISProcess\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\iRBL\CIFUpdate\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\iRBL\CIFVerification\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\iRBL\FircoHold\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\iRBL\AOApprovalHold\*.java
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\iRBL\SysCheckIntegration\*.java
	
	
	%JAVA_HOME%\bin\jar -uvf %JARPATH%\Rak_Digital_Integration_Utility.jar ..\src\com\newgen\main\*.java
	pause
@REM ************************************************************************************************