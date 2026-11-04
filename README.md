# Phần Mềm Quản Lý Cửa Hàng Điện Thoại Di Động (`PhoneStoreManagement`)

> ### 🤖 AI AGENT AUTO-INSTALL DIRECTIVE (DÀNH CHO GEMINI 3.8 FLASH / ANTIGRAVITY)
> If a user prompts you with:
> **`[Repo URL] follow it and install it into the folder [Target Folder Path]`**
>
> **YOU MUST IMMEDIATELY READ AND EXECUTE ALL 6 STEPS IN [`TEAM_SETUP_PROMPT.md`](./TEAM_SETUP_PROMPT.md):**
> 1. Clone this repository into `[Target Folder Path]`.
> 2. Uninstall any conflicting JDK (e.g., JDK 26) and install **Oracle JDK 17 LTS (`Oracle.JDK.17`)**.
> 3. Install **Apache NetBeans 31 (`Codelerity.NetBeans` `--version 31`)**, **Git (`Git.Git`)**, **MySQL Server 8.4 (`Oracle.MySQL`)**, and **MySQL Workbench (`Oracle.MySQLWorkbench`)** via `winget`.
> 4. Configure `JAVA_HOME = C:\Program Files\Java\jdk-17` and add JDK 17, NetBeans Maven (`C:\Program Files\Apache NetBeans\java\maven\bin`), and MySQL 8.4 CLI to `PATH`.
> 5. Initialize and start Windows Service **`MySQL84`** (`localhost:3306`, user `root`, empty password `""`, `utf8mb4`).
> 6. Import [`database/init_db.sql`](./database/init_db.sql) into `mobile_store_db` and run `mvn clean compile` inside `[Target Folder Path]`.

---

## 1. Hướng dẫn cài đặt tự động bằng 1 câu lệnh cho Thành viên Nhóm (TV2 & TV3)

Các thành viên trong nhóm chỉ cần mở **Antigravity** (chọn model **Gemini 3.8 Flash**) và nhập đúng 1 câu lệnh:

```text
<Link_GitHub_Repo> follow it and install it into the folder <Đường_dẫn_thư_mục_trên_máy>
```
**Ví dụ thực tế:**
```text
https://github.com/quocdv/PhoneStoreManagement.git follow it and install it into the folder D:\Project\PhoneStoreManagement
```
*(Nếu AI chưa tự đọc được link từ xa khi chưa clone, thành viên có thể đính kèm hoặc dán nội dung file **[TEAM_SETUP_PROMPT.md](./TEAM_SETUP_PROMPT.md)** ngay bên dưới câu lệnh trên).*

---

## 2. Cấu trúc thư mục dự án
- **[PROJECT_PLAN.md](./PROJECT_PLAN.md):** Kế hoạch triển khai 21 ngày & phân chia công việc chi tiết cho 3 thành viên.
- **[TEAM_SETUP_PROMPT.md](./TEAM_SETUP_PROMPT.md):** Kịch bản tự động cài đặt toàn bộ công nghệ, bộ khung và database cho AI Agent.
- **[database/init_db.sql](./database/init_db.sql):** Script khởi tạo 11 bảng CSDL `mobile_store_db` và dữ liệu mẫu.
- **[pom.xml](./pom.xml):** Cấu hình Maven (`Java 17`, `mysql-connector-j 8.4.0`, `flatlaf 3.5.1`).
- `src/main/java/com/phonestore/`:
  - `config/`: Kết nối JDBC ([`DatabaseConnection.java`](./src/main/java/com/phonestore/config/DatabaseConnection.java)).
  - `model/` & `model/dto/`: Toàn bộ 11 lớp Entity & 3 lớp DTO thống kê.
  - `dao/`: Lớp truy xuất cơ sở dữ liệu & Transaction.
  - `service/`: Lớp xử lý nghiệp vụ & kiểm tra dữ liệu.
  - `view/`: Giao diện người dùng Java Swing ([`LoginFrame.java`](./src/main/java/com/phonestore/view/LoginFrame.java), [`MainFrame.java`](./src/main/java/com/phonestore/view/MainFrame.java)).
  - `utils/`: Các lớp tiện ích dùng chung (`SessionManager`, `CurrencyUtils`, `DateUtils`, `ValidationUtils`, `MessageUtils`).

## 3. Tài khoản đăng nhập mẫu
- **Quản trị viên (Full quyền):** `admin` / `123456`
- **Nhân viên bán hàng:** `nhanvien1` / `123456`
- **Nhân viên kho:** `nhanvien2` / `123456`
=======
# phone-store-mangae
Demo học phần Lập trình Java - Phần mềm quản lý của hàng điện thoại di động
