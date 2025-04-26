package com.dacs.quanlyhocvien.Utils;

import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaExtractor {

    public DatabaseSchemaExtractor() {
    }

    public String getCompleteSchema() {
        return """
        -- Table: account
        CREATE TABLE account (
            account_id INT PRIMARY KEY,
            username VARCHAR(255),
            email VARCHAR(255),
            password VARCHAR(255),
            full_name VARCHAR(255),
            date_of_birth DATE,
            phone_number VARCHAR(20),
            address VARCHAR(255),
            gender VARCHAR(10),
            role_id INT,
            is_active BOOLEAN,
            created_at DATETIME,
            updated_at DATETIME,
            avatar_path VARCHAR(255),
            is_email_verified BOOLEAN
        );

        -- Table: admin
        CREATE TABLE admin (
            admin_id INT PRIMARY KEY,
            admin_role VARCHAR(255),
            access_level VARCHAR(255)
        );

        -- Table: roles
        CREATE TABLE roles (
            role_id INT PRIMARY KEY,
            role_name VARCHAR(255),
            description VARCHAR(255),
            created_at DATETIME
        );

        -- Table: student
        CREATE TABLE student (
            student_id INT PRIMARY KEY,
            class_name VARCHAR(255)
        );

        -- Table: teacher
        CREATE TABLE teacher (
            teacher_id INT PRIMARY KEY,
            subject_specialization VARCHAR(255),
            qualification VARCHAR(255),
            hire_date DATE
        );

        -- Foreign Keys
        ALTER TABLE account
            ADD FOREIGN KEY (role_id) REFERENCES roles(role_id);
        """;
    }
}
