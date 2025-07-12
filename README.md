# 🚀 MotorPH Payroll System  
*Advanced Object‑Oriented Programming (AOOP) • Group 9 • Java Swing • MySQL • JasperReports*

---

## 👥 Project Team
| Member | Role(s) |
|--------|---------|
| **Abegail Imee Enriquez** | Internal QA Lead • JUnit Test Engineer • Database Architect • Project Representative |
| **Alvin Tubtub**          | External QA Lead • Documentation Lead |
| **Oliver Jann Klein Borre** | **Lead Developer** • Java Swing & MySQL Integration |

---

## 🔍 Why This Exists
The first MotorPH payroll prototype buckled under 25 users — manual CSV edits, broken formulas, zero audit trail.  
This **AOOP refactor** delivers:

* ✅ **Normalized MySQL schema** — referential integrity for Attendance, Leave, Overtime, Payroll, Users.  
* ✅ **Role‑locked dashboards** — each click filtered by account privilege.  
* ✅ **One‑click PDF reports** — payslips & summaries via JasperReports.  
* ✅ **Clean layered code** — POJO → DAO → Service → UI; tests on top.

---

## 🖥️ System Requirements
| Layer | Minimum | Notes |
|-------|---------|-------|
| **JDK** | 23 (Temurin 23.x) | Compile & run |
| **IDE** | NetBeans 23+ | Maven project auto‑imports |
| **DB**  | MySQL 8.3 | Include `mysql‑connector‑j 8.4.x` |
| **Reporting** | JasperReports 6.20+ | Already in `pom.xml` |
| **OS** | Windows 10/11 or modern Linux | macOS works but not QA‑certified |

---

## 🔐 Default Test Logins
| Role | User ID | Password |
|------|---------|----------|
| IT         | `U10005` | `Hernandez@10005` |
| HR         | `U10006` | `Villanueva@10006` |
| Manager    | `U10002` | `Lim@10002` |
| Finance    | `U10011` | `Salcedo@10011` |
| Employee   | `U10008` | `Romualdez@10008` |

> ⚠️ Use only for local testing. Replace in production.

---

## 📦 Installation & First Run

### 1. Clone  
```bash
Open Terminal or Git Bash then type this to download:
git clone https://github.com/your‑org/motorph‑payroll.git
```

### 2. Import Database  
1. Open **MySQL Workbench** → *Server ▸ Data Import*.  
2. Select the SQL dump from **Milestone 2 SQL File** → import.  
3. Confirm 30+ tables appear under `motorph_payroll`.

### 3. Configure Connection  
Edit `src/main/java/db/DatabaseConnection.java`:
```java
private static final String URL      = "jdbc:mysql://localhost:3306/payrollsystem_db";
private static final String USER     = "root";
private static final String PASSWORD = "your‑mysql‑password"; ⬅️ Please update with your own DB password
```

### 4. Build  
NetBeans ▸ right‑click project ▸ **Clean and Build**.  
Unit tests pass ➜ fat JAR under `target/`.

### 5. Run  
Hit **▶️** or *Right‑click ▸ Run Project*.  
Login with the test credentials and explore.

---

## 🏗️ Project Layout
```
db/             Singleton MySQL connector
pojo/           Plain objects (Employee, Attendance, …)
dao/            CRUD interfaces
daoimpl/        JDBC implementations
service/        Business logic (validation, calculations)
ui/             Swing windows (role‑filtered)
ui/base/        Abstract Swing templates & form helpers
util/           SessionManager + misc helpers
reports/        *.jrxml Jasper templates (layout only)
test/           JUnit5 tests – NOT shipped to users
```

---

## 🛠️ Dev Tips
* **Password change:** switch it once in `DatabaseConnection.java`.  
* **New report:** drop a `.jrxml` in `reports/` and call from a Service.  
* **Extra roles:** insert into `userrole`; the UI auto‑adjusts.  
* **Unit tests:** `mvn test` covers DAO + Service happy‑paths.

---

## 📚 Docs & Assets
* 📄 **Expanded System Design Documentation:** <https://docs.google.com/spreadsheets/d/1O2_Qsl-e7WOu_GajDM0SVfFg9hynrvFhv8UL3yjqZXo>  
* ✅ **Testing Documentation** <https://docs.google.com/spreadsheets/d/1CtGctWMrtfvwRpn_sDlLcfvddDmkd6QHmhEyFHldKI8>  
* 💾 **SQL File:** <https://drive.google.com/drive/folders/1a-sITa1VIOpG-iBSz1WXD7cqRmVt97yz>

---

## 🤝 Contributing
Fixes welcome — features need a design ticket first.  
1. Fork → feature branch → PR.  
2. Respect package boundaries; business logic lives in Services, not UI.  
3. `mvn test` must be green before review.

---

## 📝 License
© 2025 MotorPH IT Group 9 — academic use permitted; commercial deployment requires written consent.

---

Made with ☕, deadlines, and just enough 🧂 to keep it honest.
