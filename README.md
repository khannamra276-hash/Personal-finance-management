# Online Personal Finance Management Platform

A standalone desktop personal finance application built with **Java 17**, **Java Swing (FlatLaf)**, **JDBC**, and **MySQL**, designed to help users track income and expenses, monitor monthly category budgets with automated warning thresholds, manage savings goals, and visualize cash flows through an interactive financial dashboard.

---

## 1. Architecture and Design Principles

The application is engineered strictly following clean architecture and the Data Access Object (DAO) pattern:

- **`com.financeapp.model`**: Pure domain entities (`User`, `Category`, `Transaction`, `Budget`, `Goal`). No framework dependencies, no ORM, no SQL. Monetary values are strictly stored and computed using `java.math.BigDecimal` to eliminate floating-point rounding errors.
- **`com.financeapp.dao`**: Pure data access layer executing SQL via `PreparedStatement` within try-with-resources blocks. Prepared statements prevent SQL injection attacks. DAOs remain completely free of business logic.
- **`com.financeapp.service`**: Encapsulates business rules:
  - Validating user inputs and hashing passwords via salted **jBCrypt**.
  - Enforcing budget threshold alerts: detecting when expense additions push category spending beyond **80%** (warning) or **100%** (exceeded).
  - Computing dashboard aggregates (total income, total expenses, net balance, category breakdown).
  - Tracking savings goals progress and contributions.
  - Exporting filtered transactions to standard CSV.
- **`com.financeapp.ui`**: Swing presentation components styled with **FlatLaf**. Kept decoupled from raw database queries, using service layer abstractions.
- **`com.financeapp.util`**: Database connection pool and configuration manager (`DBConnection`), user session state (`Session`), and centralized validators (`InputValidator`).

---

## 2. Tech Stack

- **Java**: Version 17+ (tested with OpenJDK 21)
- **Build Tool**: Apache Maven
- **UI Framework**: Java Swing with FlatLaf Light Look & Feel
- **Database**: MySQL Server 8.0+ via `mysql-connector-j` (8.3.0)
- **Security**: jBCrypt 0.4
- **Charts & Visualization**: JFreeChart 1.5.4

---

## 3. Database Schema

The database schema and sample seed data are maintained in [`src/main/resources/schema.sql`](src/main/resources/schema.sql):

- **`users`**: User identity, unique email, BCrypt password hash, timestamp.
- **`categories`**: User-scoped income and expense categories.
- **`transactions`**: High-precision monetary entries with date, category link, description, and type.
- **`budgets`**: Monthly spending limits per category, indexed by month and year.
- **`goals`**: Savings targets with deadlines, current accumulated amounts, and completion statuses.

---

## 4. Setup and Installation

### Prerequisites
1. **JDK 17 or higher** installed.
2. **MySQL Server 8.0+** running locally on port 3306.
3. **Apache Maven 3.8+** (included in build environment).

### Database Initialization
1. In MySQL Workbench or MySQL CLI, execute the script:
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```
2. Verify that `src/main/resources/config.properties` has your local MySQL username and password:
   ```properties
   db.url=jdbc:mysql://localhost:3306/personal_finance_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=your_mysql_password
   ```
   *(Note: You can also configure database credentials directly inside the application GUI via the "Database Settings" button on the login screen).*

### Running the Application

You can compile and run using Maven:
```bash
mvn clean compile exec:java -Dexec.mainClass="com.financeapp.Main"
```

Or run the packaged fat JAR directly:
```bash
mvn package
java -jar target/personal-finance-platform-1.0.0.jar
```

Alternatively, on Windows, double-click `run.bat` in the project root.

---

## 5. Seed Demonstration Account

The schema includes realistic seed data ready for presentation:
- **Email**: `alex@financeapp.com`
- **Password**: `Password123`

---

## 6. Features Walkthrough

### 1. Authentication
- User registration with validation (email regex, min 6-character password).
- Salted BCrypt password hashing.
- Active user session management.

### 2. Transactions
- Income and expense recording with date pickers and category assignment.
- Search and multi-criteria filtering (Type, Category, Date range, Description keyword).
- Table presentation with color-coded monetary values (green for income, red for expenses).
- One-click CSV export.

### 3. Monthly Budgets
- Set category-specific limits for any chosen month and year.
- Dynamic calculation of actual spending vs limit.
- Visual progress bar with status color-coding:
  - **Green**: Under 80% of budget.
  - **Amber / Orange**: Spending between 80% and 99.9%.
  - **Red**: Spending has exceeded 100% of the budget.
- Automatic alert popup when an expense pushes category spending above 80% or 100%.

### 4. Savings Goals
- Set target amount, current savings, and target completion deadlines.
- Progress bar displaying percentage completion.
- Quick "Add Contribution" dialog to fund goals over time.
- Automatic status update to `COMPLETED` once target amount is reached.

### 5. Interactive Dashboard
- Total income, total expenses, and net balance summary cards.
- **Spending by Category**: Interactive JFreeChart Pie Chart showing proportion of expenditures.
- **Budget vs Actual**: JFreeChart Clustered Bar Chart comparing budget limits against actual spending.
- Quick progress indicators for active savings goals.

