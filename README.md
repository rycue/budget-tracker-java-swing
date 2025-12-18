0. Copy the SQL code below.

1. Open Database: Click `jtvi_budget_tracker_db` in the left sidebar.
   - **Note:** If the database does not exist yet, click ***"New"*** at the top of the sidebar, type the name `jtvi_budget_tracker_db`, and click "Create."

3. Open SQL Tab: Click the ***SQL tab*** in the top navigation menu.

4. Paste & Go: Paste your code into the box and click the ***Go button*** (bottom right).

5. Verify: Ensure the 4 tables appear on the left.

```sql
-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS jtvi_budget_tracker_db;
USE jtvi_budget_tracker_db;

-- 2. Drop existing tables in reverse order of dependencies
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS goals;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 3. Users Table
CREATE TABLE users (
    user_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARBINARY(255) NOT NULL,
    secret_question VARCHAR(255),
    secret_answer VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. Categories Table
CREATE TABLE categories (
    category_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. Goals Table
CREATE TABLE goals (
    goal_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11),
    title VARCHAR(255) NOT NULL,
    target_amount DECIMAL(10, 4) NOT NULL,
    current_amount DECIMAL(10, 4) DEFAULT 0.0000,
    deadline DATE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. Transactions Table
CREATE TABLE transactions (
    transaction_id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11),
    category_id INT(11),
    amount DECIMAL(10, 4) NOT NULL,
    note VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL
) ENGINE=InnoDB;
```
