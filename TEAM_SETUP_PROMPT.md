# AI AGENT AUTO-INSTALL & ENVIRONMENT SETUP INSTRUCTIONS (`TEAM_SETUP_PROMPT.md`)

> **HOW TEAM MEMBERS TRIGGER THIS FILE:**
> Thành viên trong nhóm chỉ cần mở Antigravity (model **Gemini 3.8 Flash**) và nhập câu lệnh duy nhất:
> ```text
> [Link GitHub Repo] follow it and install it into the folder [Đường dẫn thư mục đích trên máy]
> ```
> *(Ví dụ: `https://github.com/quocdv/PhoneStoreManagement follow it and install it into the folder D:\DoAnJava\PhoneStoreManagement`)*

---

## DIRECTIVE FOR AI AGENT (GEMINI 3.8 FLASH)

When the user provides a GitHub repository URL (`<REPO_URL>`) and a target local folder path (`<TARGET_DIR>`) with the instruction *"follow it and install it into the folder..."*, you **MUST** execute all 6 steps below automatically using PowerShell (`run_command`) without stopping to ask for confirmation on basic steps.

---

### TARGET TECH STACK SPECIFICATION (MUST MATCH 100% WITH TEAM LEAD)

| Component | Exact Package / Version | Winget Package ID | Expected Install Path |
| :--- | :--- | :--- | :--- |
| **Git** | Git for Windows (`2.49+`) | `Git.Git` | `C:\Program Files\Git` |
| **Java JDK** | **Oracle JDK 17 LTS (`17.0.12`)** | `Oracle.JDK.17` | `C:\Program Files\Java\jdk-17` |
| **IDE** | **Apache NetBeans version 31** | `Codelerity.NetBeans` (`--version 31`) | `C:\Program Files\Apache NetBeans` |
| **Build Tool** | **Apache Maven `3.9.16`** (Bundled in NetBeans 31) | *(Included in `Codelerity.NetBeans`)* | `C:\Program Files\Apache NetBeans\java\maven\bin` |
| **DB Server** | **MySQL Server `8.4.9` LTS** | `Oracle.MySQL` | `C:\Program Files\MySQL\MySQL Server 8.4` |
| **DB GUI** | **MySQL Workbench `26.10.0`** | `Oracle.MySQLWorkbench` | `C:\Program Files\MySQL\MySQL Workbench 8.0 CE` |

- **MySQL Connection Standard (matches `src/main/java/com/phonestore/config/DatabaseConnection.java`):**
  - Windows Service Name: `MySQL84` (`Running`, `Automatic`)
  - Host: `localhost`, Port: `3306`
  - Database Name: `mobile_store_db`
  - Username: `root`, Password: `""` *(empty password)*
  - Character Set: `utf8mb4` (`utf8mb4_unicode_ci`), Timezone: `+07:00`

---

### STEP-BY-STEP AUTOMATED EXECUTION PIPELINE

#### STEP 1: Ensure Git is Installed & Clone the Repository into `<TARGET_DIR>`
1. Check if `git` is available (`git --version`). If not installed, install it first:
   ```powershell
   winget install --id Git.Git --exact --accept-package-agreements --accept-source-agreements
   ```
2. Create parent directory if needed and clone `<REPO_URL>` into `<TARGET_DIR>` (if `<TARGET_DIR>\pom.xml` does not already exist):
   ```powershell
   if (-not (Test-Path "<TARGET_DIR>\pom.xml")) {
       New-Item -ItemType Directory -Path (Split-Path -Parent "<TARGET_DIR>") -Force | Out-Null
       git clone <REPO_URL> "<TARGET_DIR>"
   } else {
       Set-Location "<TARGET_DIR>"
       git pull
   }
   ```

#### STEP 2: Standardize Java to Oracle JDK 17 LTS (`17.0.12`)
1. Inspect `winget list --name "JDK"` and `C:\Program Files\Java`.
2. If any non-17 Oracle JDK (e.g., `Oracle.JDK.21`, `Oracle.JDK.25`, `Oracle.JDK.26`) is installed, uninstall it and remove its leftover directory in `C:\Program Files\Java`:
   ```powershell
   # Example if Oracle.JDK.26 is present:
   winget uninstall --id Oracle.JDK.26 --exact --accept-source-agreements
   Remove-Item "C:\Program Files\Java\jdk-26*" -Recurse -Force -ErrorAction SilentlyContinue
   ```
3. Install **Oracle JDK 17 LTS**:
   ```powershell
   winget install --id Oracle.JDK.17 --exact --accept-package-agreements --accept-source-agreements
   ```

#### STEP 3: Install Apache NetBeans 31 (`Codelerity.NetBeans`), MySQL Server 8.4 & MySQL Workbench
> **CRITICAL:** Use `--id Codelerity.NetBeans --version 31` to install Apache NetBeans 31 (do NOT use `Apache.NetBeans`, which is version 25).

```powershell
# 1. Install Apache NetBeans 31 (includes Apache Maven 3.9.16)
if (-not (Test-Path "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd")) {
    winget install --id Codelerity.NetBeans --version 31 --exact --accept-package-agreements --accept-source-agreements
}

# 2. Install MySQL Server 8.4.9 LTS
if (-not (Test-Path "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe")) {
    winget install --id Oracle.MySQL --exact --accept-package-agreements --accept-source-agreements
}

# 3. Install MySQL Workbench 26.10.0
winget install --id Oracle.MySQLWorkbench --exact --accept-package-agreements --accept-source-agreements
```

#### STEP 4: Configure `JAVA_HOME` and `PATH` Environment Variables
Execute this exact PowerShell script to configure `JAVA_HOME` and add JDK 17, NetBeans Maven, and MySQL 8.4 CLI to `PATH`:

```powershell
$jdk17 = "C:\Program Files\Java\jdk-17"
$jdkBin = "$jdk17\bin"
$mavenBin = "C:\Program Files\Apache NetBeans\java\maven\bin"
$mysqlBin = "C:\Program Files\MySQL\MySQL Server 8.4\bin"

[Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk17, "User")
try { [Environment]::SetEnvironmentVariable("JAVA_HOME", $jdk17, "Machine") } catch {}

$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
$pathParts = $userPath -split ";" | Where-Object { $_ -and ($_ -notmatch "jdk-2[0-9]") }
foreach ($binPath in @($jdkBin, $mavenBin, $mysqlBin)) {
    if ($pathParts -notcontains $binPath) {
        $pathParts += $binPath
    }
}
[Environment]::SetEnvironmentVariable("Path", (($pathParts | Select-Object -Unique) -join ";"), "User")

$env:JAVA_HOME = $jdk17
$env:Path = "$jdkBin;$mavenBin;$mysqlBin;$env:Path"
```

#### STEP 5: Initialize & Start `MySQL84` Service (`localhost:3306`, `root` empty password, `utf8mb4`)
Execute this PowerShell script to initialize the MySQL data directory and start the `MySQL84` Windows service if port `3306` is not yet running:

```powershell
$mysqlHome = "C:\Program Files\MySQL\MySQL Server 8.4"
$mysqld = "$mysqlHome\bin\mysqld.exe"
$mysqlDataDir = "C:\ProgramData\MySQL\MySQL Server 8.4"
$myIniPath = "$mysqlDataDir\my.ini"

if (-not (Test-Path "$mysqlDataDir\Data\mysql")) {
    New-Item -ItemType Directory -Path $mysqlDataDir -Force | Out-Null
    $myIni = @"
[mysqld]
basedir="C:/Program Files/MySQL/MySQL Server 8.4"
datadir="C:/ProgramData/MySQL/MySQL Server 8.4/Data"
port=3306
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
default-time-zone='+07:00'

[client]
port=3306
default-character-set=utf8mb4
"@
    Set-Content -Path $myIniPath -Value $myIni -Encoding ASCII
    & $mysqld --defaults-file="$myIniPath" --initialize-insecure --console
}

if (-not (Get-Service -Name "MySQL84" -ErrorAction SilentlyContinue)) {
    & $mysqld --install MySQL84 --defaults-file="$myIniPath"
}
Start-Service -Name "MySQL84"
```

#### STEP 6: Import `database/init_db.sql` & Compile Project in `<TARGET_DIR>`
1. Import the 11 tables and sample data from `<TARGET_DIR>\database\init_db.sql` into `mobile_store_db`:
   ```powershell
   $mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
   cmd /c "`"$mysqlExe`" -u root --default-character-set=utf8mb4 < `"<TARGET_DIR>\database\init_db.sql`""
   & $mysqlExe -u root --default-character-set=utf8mb4 -D mobile_store_db -e "SHOW TABLES;"
   ```
2. Run Maven build inside `<TARGET_DIR>` to download dependencies (`mysql-connector-j`, `flatlaf`) and verify compilation:
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
   Set-Location "<TARGET_DIR>"
   & "C:\Program Files\Apache NetBeans\java\maven\bin\mvn.cmd" clean compile
   ```

---

### FINAL REPORT TO DISPLAY TO THE USER
Once all 6 steps finish, output a summary table confirming:
1. **Repository Cloned Path:** `<TARGET_DIR>`
2. **Java Version & `JAVA_HOME`:** `Oracle JDK 17.0.12` (`C:\Program Files\Java\jdk-17`)
3. **IDE & Maven Version:** `Apache NetBeans 31` & `Apache Maven 3.9.16`
4. **MySQL Server Status:** Service `MySQL84` (`Running` on port `3306`, `utf8mb4`) + 11 tables created in `mobile_store_db`
5. **Project Compilation:** `mvn clean compile` $\rightarrow$ `BUILD SUCCESS`
