CREATE DATABASE IF NOT EXISTS employee_management CHARACTER SET utf8mb4;
USE employee_management;

CREATE TABLE IF NOT EXISTS department (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS employee (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    department_id BIGINT NOT NULL,
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department (id)
);

INSERT INTO department (id, name) VALUES (1, 'IT'), (2, 'HR')
ON DUPLICATE KEY UPDATE id = id;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT uk_app_user_username UNIQUE (username)
);
