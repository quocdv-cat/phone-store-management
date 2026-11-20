@echo off
setlocal EnableDelayedExpansion

:: Kiem tra quyen Administrator
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [INFO] Dang yeu cau quyen Administrator de cai dat Windows Service...
    powershell -NoProfile -Command "Start-Process cmd.exe -ArgumentList '/k \"\"%~f0\"\"' -Verb RunAs"
    exit /b
)

title Cai dat Windows Service MySQL84
echo ======================================================================
echo    DANG CAI DAT DICH VU MYSQL84 (TU DONG KHOI CHAY CUNG WINDOWS)
echo ======================================================================
echo.

:: 1. Tat tien trinh mysqld chay ngam (neu co) de tranh khoa file/cong 3306
taskkill /F /IM mysqld.exe >nul 2>&1

:: 2. Dang ky service MySQL84
echo [1/3] Dang ky MySQL84 vao he thong Windows Service...
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --install MySQL84 --defaults-file="C:\ProgramData\MySQL\MySQL Server 8.4\my.ini"

:: 3. Cau hinh tu dong khoi dong cung he thong
echo [2/3] Cau hinh tu dong khoi dong (Automatic)...
sc config MySQL84 start= auto

:: 4. Khoi chay service
echo [3/3] Dang khoi chay dich vu MySQL84...
net start MySQL84

echo.
echo ======================================================================
echo    TRANG THAI DICH VU MYSQL84:
echo ======================================================================
sc query MySQL84
echo.
echo Cai dat hoan tat! Ban co the dong cua so nay.
pause
